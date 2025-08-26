package org.rockhopper.smarhome.hsb.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Home Assistant Configuration
 */
@Component
@ConfigurationProperties("ha")
@Setter
@Getter
public class HaConfig {
	
    /**
     * Bearer Token
     */
    private String bearer;
    /**
     * HA protocol
     */
    private String protocol;    
    /**
     * HA Host
     */
    private String host;
    /**
     * HA Port (API)
     */
    private Integer port;
	public String getBearer() {
		return bearer;
	}
	public void setBearer(String bearer) {
		this.bearer = bearer;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
}