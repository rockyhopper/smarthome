package org.rockhopper.smarthome.wes.wes2mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
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
    private String clientId;
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

	public String getBaseTopic() {
		return baseTopic;
	}

	public void setBaseTopic(String baseTopic) {
		this.baseTopic = baseTopic;
	}
	
	public String getCommandSubTopic() {
		return commandSubTopic;
	}
	
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public void setCommandSubTopic(String commandSubTopic) {
		this.commandSubTopic= commandSubTopic;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setKeepalive(int keepalive) {
		this.keepalive = keepalive;
	}

	public String getStatSubTopic() {
		return statSubTopic;
	}

	public void setStatSubTopic(String statSubTopic) {
		this.statSubTopic = statSubTopic;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getHostUrl() {
		return hostUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getKeepalive() {
		return keepalive;
	} 
	
	
}