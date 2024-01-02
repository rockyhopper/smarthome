package org.rockhopper.smarhome.hsb.server;

import java.time.format.DateTimeFormatter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class MqttHsbClient implements MqttCallback, DisposableBean {
	protected Logger log= LoggerFactory.getLogger(getClass());
	
	protected boolean shutdownInProgress= false;
	
    @Autowired
    private MqttConfig mqttConfig;
        
    @Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
    
    @Autowired
    private MqttPushClient mqttPushClient;
    
    @Autowired
    private RestClient haRestClient;

	public MqttHsbClient() {
	}
	
	public void start() {
        new HomeAssistantIntegration(mqttPushClient, freeMarkerConfigurer).fulfill();
		mqttPushClient.setCallback(this);	
	}
	
	public boolean isShutdownInProgress() {
		return shutdownInProgress;
	}
	
	public void stop() {
		shutdownInProgress= true;
				
		if (mqttPushClient!=null) {
			mqttPushClient.close();
		}			
	}
	
	/*
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

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.info("Receive message subject : " + topic);
		log.info("receive messages Qos : " + message.getQos());
        log.info("Receive message content : " + message.getPayload());
        log.info("Receive message as String : " + new String (message.getPayload()));
        
		if (message.getPayload()==null) {
			return;
		}		
		if (topic.startsWith(mqttConfig.getHsbBaseTopic())){			
			String payload= new String();
			log.info("{}>>{}",topic, payload);
		}
		else if (topic.startsWith(mqttConfig.getSmsBaseTopic())){
			String subTopic= StringUtils.removeStart(topic, mqttConfig.getSmsBaseTopic() + "/");
			switch (subTopic){
				case "connected":
					log.info("{}>>{}",topic, message.getPayload());
					break;
				case "received":
					String payload= new String(message.getPayload());
					
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.registerModule(new JavaTimeModule());
					
					SMS sms= objectMapper.readValue(payload, SMS.class);
					
					ResponseEntity<Void>  response= haRestClient.post().uri("https://ha.rockhopper.org:8123/api/services/notify/mobile_app_rmx3472")
									   					.body("{\"message\": \"At %s from %s: %s\"}".formatted(DateTimeFormatter.ISO_LOCAL_TIME.format(sms.getDatetime()),sms.getNumber(),sms.getText()))
									   					.retrieve()
									   					.toBodilessEntity();
					System.out.println(response.getStatusCode().value());
					break;
				case "signal":
					log.info("{}>>{}",topic, message.getPayload());
					break;
				default:
			
			}
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("deliveryComplete---------" + token.isComplete());
	}

	@Override
	public void destroy() throws Exception {
		log.info("MqttHsbClient#destroy() is stopping MQTT HSB Client!");
		if (!shutdownInProgress) {
			stop();
		} 
	}
}
