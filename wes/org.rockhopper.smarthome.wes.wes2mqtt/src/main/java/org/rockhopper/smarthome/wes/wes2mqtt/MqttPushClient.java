package org.rockhopper.smarthome.wes.wes2mqtt;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component
public class MqttPushClient {
    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

	protected boolean shutdownInProgress= false;
    
    private static MqttClient client;
    
    // Store the callback to restore it after reconnection
    private MqttCallback storedCallback;
    
    @Autowired
    private MqttConfig mqttConfig;
    
    private String statTopic;

    private MqttClient getClient() {
        return client;
    }

    private static void setClient(MqttClient client) {
        MqttPushClient.client = client;
    }

    /**
     * Client connection
     *
     * @param host      ip+port
     * @param clientID  Client Id
     * @param username  User name
     * @param password  Password
     * @param statTopic Stat Topic
     * @param timeout   Timeout time
     * @param keepalive Retention number
     */
    public void connect(String host, String clientID, String username, String password, String statTopic, int timeout, int keepalive) throws MqttException {
    	if (shutdownInProgress) {
    		throw new MqttException(MqttException.REASON_CODE_CLIENT_DISCONNECTING);
    	}
    	this.statTopic= statTopic;
    	
        logger.info("Connecting to MQTT broker at {} with client ID {}", host, clientID);
        
        client = new MqttClient(host, clientID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(timeout);
        options.setKeepAliveInterval(keepalive);
        
        // Enable automatic reconnection
        options.setAutomaticReconnect(false); // We handle reconnection manually for better control
        
        setClient(client);
        
        // Set callback if we have one stored
        if (storedCallback != null) {
        	client.setCallback(storedCallback);
        }
        
        client.connect(options);
        
        if (!client.isConnected()) {
        	throw new MqttException(MqttException.REASON_CODE_BROKER_UNAVAILABLE);
        }
        
        logger.info("Successfully connected to MQTT broker");
    }

    public void close(){
    	shutdownInProgress= true;
    	if (client!=null) {
    		try {
    			if (client.isConnected()) {
    				client.disconnectForcibly();
    			}
				client.close();
			} 
    		catch (MqttException me) {
    			logger.warn("Exception caught while closing (MQTT) Client", me);
			}
    	}
    }
    
    public void setCallback(MqttCallback mqttCallback) {
    	this.storedCallback = mqttCallback;
    	if (client != null) {
    		client.setCallback(mqttCallback);
    	}
	}
    
    public void publishToTopic(int qos, boolean retained, String topic, String pushMessage) throws MqttException {
    	if (shutdownInProgress) {
    		throw new MqttException(MqttException.REASON_CODE_CLIENT_DISCONNECTING);
    	}
    	
    	if (client == null || !client.isConnected()) {
    		throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
    	}
    	
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());
        MqttTopic mTopic = getClient().getTopic(topic);
        if (null == mTopic) {
            logger.error("topic not exist");
            throw new MqttException(MqttException.REASON_CODE_INVALID_MESSAGE);
        }
        MqttDeliveryToken token = mTopic.publish(message);
        token.waitForCompletion();
    }
    /**
     * Release
     *
     * @param qos         Connection mode
     * @param retained    Whether to retain
     * @param subtopic    SubTopic
     * @param pushMessage Message body
     */
    public void publishToSubTopic(int qos, boolean retained, String subtopic, String pushMessage) throws MqttException {
    	publishToTopic(qos, retained, statTopic + "/" + subtopic, pushMessage);
    }

    /**
     * Subscribe to a topic
     *
     * @param commandtopic Command Topic
     * @param qos The maximum quality of service at which to subscribe.
     */
    public void subscribe(String commandtopic, int qos) {
        logger.info("Start subscribing to topics {}", commandtopic);
        try {
            getClient().subscribe(commandtopic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    
    public void reconnectMqttPushClient() throws MqttException {
    	if (shutdownInProgress) {
    		throw new MqttException(MqttException.REASON_CODE_CLIENT_DISCONNECTING);
    	}
    	
    	logger.info("Attempting MQTT reconnection...");
    	logger.debug("hostUrl: {}", mqttConfig.getHostUrl());
    	logger.debug("clientID: {}", mqttConfig.getClientId());
    	logger.debug("username: {}", mqttConfig.getUsername());
    	logger.debug("timeout: {}", mqttConfig.getTimeout());
    	logger.debug("keepalive: {}", mqttConfig.getKeepalive());
    	
    	// Cleanup existing connection if any
    	if (client != null) {
    		try {
    			if (client.isConnected()) {
    				client.disconnectForcibly();
    			}
    			client.close();
    		} catch (Exception e) {
    			logger.warn("Error cleaning up existing MQTT client: {}", e.getMessage());
    		}
    	}
    	
        connect(mqttConfig.getHostUrl(), 
			    mqttConfig.getClientId(), 
			    mqttConfig.getUsername(),
			    mqttConfig.getPassword(), 
			    mqttConfig.getBaseTopic() + "/" + mqttConfig.getStatSubTopic(), 
			    mqttConfig.getTimeout(), 
			    mqttConfig.getKeepalive());
        
        // Restore the callback on the new client
        if (storedCallback != null) {
        	logger.debug("Restoring MQTT callback after reconnection");
        	client.setCallback(storedCallback);
        }
			    
        if (!isConnected()) {
        	throw new MqttException(MqttException.REASON_CODE_BROKER_UNAVAILABLE);
        }
        
        subscribe(mqttConfig.getBaseTopic() + "/" + mqttConfig.getCommandSubTopic() + "/#", 0);
        logger.info("MQTT reconnection successful");
    }
    
    /**
     * Check if MQTT client is connected
     */
    public boolean isConnected() {
    	return client != null && client.isConnected();
    }
    
    /**
     * Get connection status information
     */
    public String getConnectionStatus() {
    	if (client == null) {
    		return "Client not initialized";
    	}
    	return client.isConnected() ? "Connected" : "Disconnected";
    }
    
    @PostConstruct
    public void init() {
    	try {
    		reconnectMqttPushClient();
    	} catch (MqttException e) {
    		logger.error("Failed to initialize MQTT connection: {}", e.getMessage(), e);
    		// Don't throw exception here to allow application to start
    		// The reconnection mechanism will handle retries
    	}
    }
}