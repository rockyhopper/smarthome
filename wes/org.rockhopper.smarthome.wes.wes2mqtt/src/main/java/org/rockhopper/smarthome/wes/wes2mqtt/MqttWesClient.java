package org.rockhopper.smarthome.wes.wes2mqtt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.rockhopper.smarthome.wes.jwes.model.WesEvent;
import org.rockhopper.smarthome.wes.jwes.model.WesEvent.WesEventCode;
import org.rockhopper.smarthome.wes.jwes.model.WesEventListener;
import org.rockhopper.smarthome.wes.jwes.model.WesServer;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesOneWireRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCard;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesDataNavigatorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Component
public class MqttWesClient implements WesEventListener, MqttCallback, DisposableBean {
	protected Logger log= LoggerFactory.getLogger(getClass());
	
	protected boolean shutdownInProgress= false;
	
	// MQTT Reconnection management
	private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "MQTT-Reconnect-Thread");
		t.setDaemon(true);
		return t;
	});
	private ScheduledFuture<?> reconnectTask;
	private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
	private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
	
	// Reconnection configuration
	private static final int MAX_RECONNECT_ATTEMPTS = 10;
	private static final long INITIAL_RECONNECT_DELAY_MS = 1000; // 1 second
	private static final long MAX_RECONNECT_DELAY_MS = 60000; // 1 minute
	private static final double BACKOFF_MULTIPLIER = 2.0;
	
    @Autowired
    private MqttConfig mqttConfig;
        
    @Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
    
    @Autowired
    private MqttPushClient mqttPushClient;
    	
	private Map<String,Field<?, ?>> cmndLabels;
	
	private WesServer wesServer;	
	
	public MqttWesClient() {
	}
	
	public void start() {
		if (wesServer==null) {
			throw new IllegalStateException("MqttWesClient cannot be started without WES Server!");
		}
		
		log.info("{} {}", wesServer.getWesData(), (wesServer.getWesConfig()!=null)?wesServer.getWesConfig().getIpAddress():null);
		
		
		Set<Field<?, ?>> fields = wesServer.label();		
        fields.forEach(field -> {
            onEvent(new WesEvent(field.getLabel(), WesEvent.WesEventCode.UPDATE, null, field.getValue()));
        });
        
        WesData wesData= wesServer.getWesData();
        
        cmndLabels= new HashMap<String,Field<?, ?>>();
        String cmndTopic= mqttConfig.getBaseTopic() + "/" + mqttConfig.getCommandSubTopic() + "/";       
        cmndLabels.put(cmndTopic + labelToSubTopic(wesData.getRelay1().getValue().getLabel()), wesData.getRelay1().getValue());
        log.info("Adding cmdTopic '{}'", cmndTopic + labelToSubTopic(wesData.getRelay1().getValue().getLabel()));
        cmndLabels.put(cmndTopic + labelToSubTopic(wesData.getRelay2().getValue().getLabel()), wesData.getRelay2().getValue());        
        log.info("Adding cmdTopic '{}'", cmndTopic + labelToSubTopic(wesData.getRelay2().getValue().getLabel()));
        
        List<WesRelaysCard> relaysCardsLists= wesData.getRelaysCards().getCards();
        if ((relaysCardsLists!=null)&&(relaysCardsLists.size()>0)) {
        	Iterator<WesRelaysCard> relaysCardsListsIt= relaysCardsLists.iterator();
        	while (relaysCardsListsIt.hasNext()) {
        		WesRelaysCard relaysCard= relaysCardsListsIt.next();
        		List<WesOneWireRelay> oneWireRelaysList= relaysCard.getRelays();
                if ((oneWireRelaysList!=null)&&(oneWireRelaysList.size()>0)) {
                	Iterator<WesOneWireRelay> oneWireRelayIt= oneWireRelaysList.iterator();
                	while (oneWireRelayIt.hasNext()) {
                		WesOneWireRelay oneWireRelay= oneWireRelayIt.next();
                		if (oneWireRelay!=null) {
                			cmndLabels.put(cmndTopic + labelToSubTopic(oneWireRelay.getState().getLabel()), oneWireRelay.getState());
                	        log.info("Adding cmdTopic '{}'", cmndTopic + labelToSubTopic(oneWireRelay.getState().getLabel()), oneWireRelay.getState());
                		}
                	}
                }
        	}
        }
        
        MapUtils.debugPrint(System.out, "myMap", cmndLabels);
        
        new HomeAssistantIntegration(mqttPushClient, freeMarkerConfigurer).fulfillDiscovery(wesServer);
        

		mqttPushClient.setCallback(this);	
		wesServer.startPolling(this);	
	}
	
	public boolean isShutdownInProgress() {
		return shutdownInProgress;
	}
	
	public void stop() {
		shutdownInProgress= true;
		
		// Cancel any pending reconnection attempts
		cancelReconnectTask();
		
		if (wesServer!=null) {
			wesServer.stopPolling();
		}
		
		if (mqttPushClient!=null) {
			mqttPushClient.close();
		}
		
		// Shutdown the reconnect executor
		if (!reconnectExecutor.isShutdown()) {
			reconnectExecutor.shutdown();
			try {
				if (!reconnectExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
					reconnectExecutor.shutdownNow();
				}
			} catch (InterruptedException e) {
				reconnectExecutor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}
	
	@Override
	public void onEvent(WesEvent event) {
		if ((WesEventCode.UPDATE.equals(event.getEventCode())) || 
			(WesEventCode.SYNC.equals(event.getEventCode()))
			){
			if (mqttPushClient!=null) {
				if (event.getNewValue()!=null) {
					try {
						mqttPushClient.publishToSubTopic(0,false,labelToSubTopic(event.getFieldLabel()), event.getNewValue().toString());
						// Reset reconnection state on successful publish (connection is working)
						if (reconnectAttempts.get() > 0) {
							resetReconnectionState();
						}
					} catch (Exception e) {
						log.warn("Failed to publish MQTT message for event {}: {}", event.getFieldLabel(), e.getMessage());
						// Don't trigger reconnection here as connectionLost will be called by the MQTT client
					}
				}
				else {
				    log.warn("Issue handling WesEvent, the new value for '{}' is null!", event.getFieldLabel());
				}
			}
			else {
				log.error("mqttPushClient is *NULL*!!!");
			}
		}
	}
	
	public String labelToSubTopic(String label) {
		if ((label==null)||(label.length()==0)){
			return label;
		}
		String subTopic= StringUtils.removeStart(label, WesDataNavigatorHelper.LABEL_DATA_PREFIX + ".");
		subTopic= StringUtils.replace(subTopic, ".", "/");
		return subTopic;
	}
	
	// To move in jwes code!!
	/*
	private WesData readXml() {
        XStream xstream = new XStream(new WstxDriver());
        xstream.processAnnotations(WesData.class);
        xstream.registerConverter(new WesRelaysCardsConverter(xstream.getMapper(), null, false));
        xstream.registerConverter(new FieldConverter(null, false));
        try (FileReader fileReader = new FileReader(new File("OUT.XML"))) {
            return (WesData) xstream.fromXML(fileReader);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
	*/

	@Override
	public void connectionLost(Throwable cause) {
		log.error("MQTT connection lost! Cause: {}, shutdownInProgress: {}", 
			(cause != null) ? cause.getMessage() : "Unknown reason", shutdownInProgress, cause);
		
		if (!shutdownInProgress) {
			log.info("Triggering automatic reconnection due to connection loss");
			scheduleReconnect();
		} else {
			log.info("Skipping reconnection attempt - shutdown in progress");
		}
	}
	
	/**
	 * Schedule a reconnection attempt with exponential backoff
	 */
	private void scheduleReconnect() {
		if (isReconnecting.compareAndSet(false, true)) {
			int currentAttempt = reconnectAttempts.incrementAndGet();
			
			if (currentAttempt > MAX_RECONNECT_ATTEMPTS) {
				log.error("Maximum reconnection attempts ({}) exceeded. Giving up reconnection.", MAX_RECONNECT_ATTEMPTS);
				isReconnecting.set(false);
				return;
			}
			
			long delay = calculateReconnectDelay(currentAttempt);
			log.info("Scheduling MQTT reconnection attempt {} in {} ms", currentAttempt, delay);
			
			reconnectTask = reconnectExecutor.schedule(this::attemptReconnect, delay, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * Calculate reconnection delay with exponential backoff
	 */
	private long calculateReconnectDelay(int attempt) {
		long delay = (long) (INITIAL_RECONNECT_DELAY_MS * Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
		return Math.min(delay, MAX_RECONNECT_DELAY_MS);
	}
	
	/**
	 * Attempt to reconnect to MQTT broker
	 */
	private void attemptReconnect() {
		if (shutdownInProgress) {
			log.info("Shutdown in progress, cancelling reconnection attempt");
			isReconnecting.set(false);
			return;
		}
		
		try {
			log.info("Attempting MQTT reconnection (attempt {})", reconnectAttempts.get());
			
			// Attempt reconnection
			mqttPushClient.reconnectMqttPushClient();
			
			// If successful, reset counters and flags
			log.info("MQTT reconnection successful after {} attempts", reconnectAttempts.get());
			reconnectAttempts.set(0);
			isReconnecting.set(false);
			
			// Re-publish Home Assistant discovery messages after successful reconnection
			if (wesServer != null) {
				log.info("Re-publishing Home Assistant discovery messages after successful MQTT reconnection");
				try {
					new HomeAssistantIntegration(mqttPushClient, freeMarkerConfigurer).fulfillDiscovery(wesServer);
					log.info("Home Assistant discovery messages successfully re-published");
				} catch (Exception discoveryException) {
					log.warn("Failed to re-publish Home Assistant discovery messages: {}", discoveryException.getMessage(), discoveryException);
					// Continue with other recovery steps even if discovery fails
				}
			}
			
			// Restart WES polling if it was stopped
			if (wesServer != null && !wesServer.isPolling()) {
				log.info("Restarting WES polling after successful MQTT reconnection");
				wesServer.startPolling(this);
			}
			
		} catch (Exception e) {
			log.warn("MQTT reconnection attempt {} failed: {}", reconnectAttempts.get(), e.getMessage());
			isReconnecting.set(false);
			
			// Schedule next attempt if we haven't exceeded max attempts
			if (reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
				scheduleReconnect();
			} else {
				log.error("All reconnection attempts failed. Manual intervention may be required.");
			}
		}
	}
	
	/**
	 * Cancel any pending reconnection task
	 */
	private void cancelReconnectTask() {
		if (reconnectTask != null && !reconnectTask.isDone()) {
			log.info("Cancelling pending MQTT reconnection task");
			reconnectTask.cancel(false);
		}
		isReconnecting.set(false);
	}
	
	/**
	 * Reset reconnection counters (can be called when connection is manually restored)
	 */
	public void resetReconnectionState() {
		log.info("Resetting MQTT reconnection state");
		cancelReconnectTask();
		reconnectAttempts.set(0);
		isReconnecting.set(false);
	}

	/**
	 * Start periodic health check to ensure connectivity
	 */
	@PostConstruct
	private void startHealthCheck() {
		log.info("Starting MQTT health check service");
		// Schedule a periodic health check every 60 seconds (more frequent than before)
		reconnectExecutor.scheduleWithFixedDelay(this::performHealthCheck, 
			10, 60, TimeUnit.SECONDS); // 10 seconds initial delay, then every 60 seconds
	}
	
	/**
	 * Perform health check and reset reconnection state if broker becomes available
	 */
	private void performHealthCheck() {
		try {
			boolean isConnected = mqttPushClient.isConnected();
			log.debug("MQTT Health check: connected={}, isReconnecting={}, attempts={}", 
				isConnected, isReconnecting.get(), reconnectAttempts.get());
			
			if (!isConnected && !isReconnecting.get()) {
				// If we're not connected and not currently trying to reconnect
				if (reconnectAttempts.get() >= MAX_RECONNECT_ATTEMPTS) {
					log.warn("Health check: Max reconnection attempts exceeded. Resetting state and retrying...");
					resetReconnectionState();
				}
				
				log.info("Health check: Triggering reconnection attempt");
				scheduleReconnect();
			}
		} catch (Exception e) {
			log.error("Error during MQTT health check: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * Get current reconnection status
	 */
	public boolean isReconnecting() {
		return isReconnecting.get();
	}
	
	/**
	 * Get current number of reconnection attempts
	 */
	public int getReconnectionAttempts() {
		return reconnectAttempts.get();
	}
	
	/**
	 * Re-publish Home Assistant discovery messages
	 * This can be called manually or after reconnection
	 */
	public void republishHomeAssistantDiscovery() {
		if (wesServer == null) {
			log.warn("Cannot republish Home Assistant discovery: WES Server is null");
			return;
		}
		
		if (!mqttPushClient.isConnected()) {
			log.warn("Cannot republish Home Assistant discovery: MQTT client is not connected");
			return;
		}
		
		try {
			log.info("Republishing Home Assistant discovery messages");
			new HomeAssistantIntegration(mqttPushClient, freeMarkerConfigurer).fulfillDiscovery(wesServer);
			log.info("Home Assistant discovery messages successfully republished");
		} catch (Exception e) {
			log.error("Failed to republish Home Assistant discovery messages: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to republish Home Assistant discovery messages", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.info("Receive message subject : " + topic);
		log.info("receive messages Qos : " + message.getQos());
        log.info("Receive message content : " + message.getPayload());
        log.info("Receive message as String : " + new String (message.getPayload()));
        
		if (message.getPayload()==null) {
			return;
		}
		
		String payload= new String(message.getPayload());
        
		if (wesServer!=null) {	
	        Field<?,?> field= cmndLabels.get(topic);
	        if (field==null) {
	        	log.error("No field matching topic '{}'", topic);
	        }
	        else if ("0".equals(payload)) {
	            log.info("forceUpdate : {} -> {}", field.getLabel(), payload);
	            wesServer.<Byte,Void>forceUpdate((Field<Byte,Void>)field, Byte.valueOf((byte)0));
	        }
	        else if ("1".equals(payload)) {
	        	log.info("forceUpdate : {} -> {}", field.getLabel(), payload);
	        	wesServer.<Byte,Void>forceUpdate((Field<Byte,Void>)field, Byte.valueOf((byte)1));
	        } 
	        else {
	        	log.info("forceUpdate : {} -> {}", field.getLabel(), payload);
	        	wesServer.<Boolean,Void>forceUpdate((Field<Boolean,Void>)field, Boolean.valueOf(payload));
	        }
		}
		else {
			log.warn("Skipping payload [{}] as there is no WES Server!");
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("deliveryComplete---------" + token.isComplete());
	}
	
	public void setWesServer(WesServer wesServer) {
		this.wesServer = wesServer;
	}

	@Override
	public void destroy() throws Exception {
		log.info("MqttWesClient#destroy() is stopping MQTT WES Client!");
		if (!shutdownInProgress) {
			stop();
		} 
	}
}
