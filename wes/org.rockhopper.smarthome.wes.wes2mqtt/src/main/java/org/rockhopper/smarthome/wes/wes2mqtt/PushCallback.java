package org.rockhopper.smarthome.wes.wes2mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQTT Listen Callback
 */
@Component
public class PushCallback implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Autowired
    private MqttPushClient mqttPushClient;
    
    private static MqttClient client;

    @Override
    public void connectionLost(Throwable cause) {
        // After the connection is lost, it is usually reconnected here
        logger.info("Disconnected, can be reconnected");
        if (null != client) {
        	mqttPushClient.reconnectMqttPushClient();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        // The message you get after you subscribe will be executed here
        logger.info("Receive message subject : " + topic);
        logger.info("receive messages Qos : " + mqttMessage.getQos());
        logger.info("Receive message content : " + new String(mqttMessage.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        logger.info("deliveryComplete---------" + iMqttDeliveryToken.isComplete());
    }
}