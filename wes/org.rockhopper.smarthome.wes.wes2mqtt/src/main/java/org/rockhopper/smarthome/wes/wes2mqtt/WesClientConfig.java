package org.rockhopper.smarthome.wes.wes2mqtt;

import org.rockhopper.smarthome.wes.jwes.model.WesConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("wes")
public class WesClientConfig extends WesConfig{
	private String macAddress;
	
	
	public WesClientConfig() {
		super(null);
	}

	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
}
