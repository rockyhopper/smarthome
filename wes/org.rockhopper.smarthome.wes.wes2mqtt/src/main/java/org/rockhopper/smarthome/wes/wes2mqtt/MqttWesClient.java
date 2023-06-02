package org.rockhopper.smarthome.wes.wes2mqtt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		
		if (wesServer!=null) {
			wesServer.stopPolling();
		}
		
		if (mqttPushClient!=null) {
			mqttPushClient.close();
		}			
	}
	
	@Override
	public void onEvent(WesEvent event) {
		if ((WesEventCode.UPDATE.equals(event.getEventCode())) || 
			(WesEventCode.SYNC.equals(event.getEventCode()))
			){
			if (mqttPushClient!=null) {
				if (event.getNewValue()!=null) {
					mqttPushClient.publishToSubTopic(0,false,labelToSubTopic(event.getFieldLabel()), event.getNewValue().toString());
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
		if (!shutdownInProgress) {
	        // After the connection is lost, it is usually reconnected here
	        log.error("Disconnected ({}).", (cause!=null)?cause.getMessage():null, cause);
	        try {
	        	mqttPushClient.reconnectMqttPushClient();
	        }
	        catch (Exception e) {
	        	log.error("Exception caught while trying to reconnect to MQTT Server", e);
	        }
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
