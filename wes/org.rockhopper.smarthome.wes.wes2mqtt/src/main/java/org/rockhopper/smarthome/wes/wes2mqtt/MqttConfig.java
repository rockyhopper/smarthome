package org.rockhopper.smarthome.wes.wes2mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * MQTT Configuration
 */
@Component
@ConfigurationProperties("mqtt")
@Setter
@Getter
public class MqttConfig {
    @Autowired
    private MqttPushClient mqttPushClient;

    /**
     * User name
     */
   // @Value("username")
    private String username;
    /**
     * Password
     */
    private String password;
    /**
     * Connection address
     */
    private String hostUrl;
    /**
     * Customer Id
     */
    private String clientID;
    /**
     * Base connection topic
     */
    private String baseTopic;
    /**
     * Commands connection subtopic (/baseTopic/commandTopic)
     */
    private String commandSubTopic;
    /**
     * Statistics connection subtopic (/baseTopic/statTopic)
     */
    private String statSubTopic;    
    /**
     * Timeout time
     */
    private int timeout;
    /**
     * Keep connected
     */
    private int keepalive;

    @Bean
    public MqttPushClient getMqttPushClient() {
    	System.out.println("hostUrl: "+ hostUrl);
    	System.out.println("clientID: "+ clientID);
    	System.out.println("username: "+ username);
    	System.out.println("password: "+ password);
    	System.out.println("timeout: "+timeout);
    	System.out.println("keepalive: "+ keepalive);
        mqttPushClient.connect(hostUrl, clientID, username, password, baseTopic + "/" + statSubTopic, timeout, keepalive);
        mqttPushClient.subscribe(baseTopic + "/" + commandSubTopic + "/#", 0);
        return mqttPushClient;
    }

    public void reconnectMqttPushClient(MqttPushClient mqttPushClient){
        mqttPushClient.connect(hostUrl, clientID, username, password, baseTopic + "/" + statSubTopic, timeout, keepalive);
        mqttPushClient.subscribe(baseTopic + "/" + commandSubTopic + "/#", 0);    	
    }

	public String getBaseTopic() {
		return baseTopic;
	}

	public void setBaseTopic(String baseTopic) {
		this.baseTopic = baseTopic;
	}
	
	public String getCommandSubTopic() {
		return commandSubTopic;
	}
	
	public void setCommandSubTopic(String commandSubTopic) {
		this.commandSubTopic= commandSubTopic;
	}

	public String getStatSubTopic() {
		return statSubTopic;
	}

	public void setStatSubTopic(String statSubTopic) {
		this.statSubTopic = statSubTopic;
	} 
	
	
}