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
    public void connect(String host, String clientID, String username, String password, String statTopic, int timeout, int keepalive) {
    	if (shutdownInProgress) {
    		return;
    	}
    	this.statTopic= statTopic;
    	
        try {
            client = new MqttClient(host, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(timeout);
            options.setKeepAliveInterval(keepalive);
            setClient(client);
            try {
                client.connect(options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
   		client.setCallback(mqttCallback);
	}
    
    public void publishToTopic(int qos, boolean retained, String topic, String pushMessage) {
    	if (shutdownInProgress) {
    		return;
    	}
    	
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());
        MqttTopic mTopic = getClient().getTopic(topic);
        if (null == mTopic) {
            logger.error("topic not exist");
        }
        MqttDeliveryToken token;
        try {
            token = mTopic.publish(message);
            token.waitForCompletion();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    /**
     * Release
     *
     * @param qos         Connection mode
     * @param retained    Whether to retain
     * @param subtopic    SubTopic
     * @param pushMessage Message body
     */
    public void publishToSubTopic(int qos, boolean retained, String subtopic, String pushMessage) {
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
    
    public void reconnectMqttPushClient(){    
    	System.out.println("hostUrl: "+ mqttConfig.getHostUrl());
    	System.out.println("clientID: "+ mqttConfig.getClientId());
    	System.out.println("username: "+ mqttConfig.getUsername());
    	System.out.println("password: "+ mqttConfig.getPassword());
    	System.out.println("timeout: " + mqttConfig.getTimeout());
    	System.out.println("keepalive: "+ mqttConfig.getKeepalive());    	
        connect(mqttConfig.getHostUrl(), 
			    mqttConfig.getClientId(), 
			    mqttConfig.getUsername(),
			    mqttConfig.getPassword(), 
			    mqttConfig.getBaseTopic() + "/" + mqttConfig.getStatSubTopic(), 
			    mqttConfig.getTimeout(), 
			    mqttConfig.getKeepalive());
        subscribe(mqttConfig.getBaseTopic() + "/" + mqttConfig.getCommandSubTopic() + "/#", 0);    	
    }
    
    @PostConstruct
    public void init() {
    	reconnectMqttPushClient();
    }
}