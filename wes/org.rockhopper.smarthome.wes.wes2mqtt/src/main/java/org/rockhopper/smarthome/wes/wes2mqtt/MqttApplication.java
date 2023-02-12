package org.rockhopper.smarthome.wes.wes2mqtt;


import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.WesClient;
import org.rockhopper.smarthome.wes.jwes.WesConstants;
import org.rockhopper.smarthome.wes.jwes.discovery.WesUtils;
import org.rockhopper.smarthome.wes.jwes.model.WesServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MqttApplication {
    private static final Logger logger= LoggerFactory.getLogger(MqttApplication.class);
    
    @Autowired
    private WesClientConfig wesConfig;
    
    @Autowired
    private MqttWesClient mqttWesClient;
    
    public static void main(String[] args) {
        SpringApplication.run(MqttApplication.class, args);
    }

	@EventListener(ApplicationReadyEvent.class)
	public void afterSpringStartup() {
		logger.info("Start...");
		
		if (StringUtils.isNotBlank(wesConfig.getIpAddress()) && (StringUtils.isBlank(wesConfig.getMacAddress()))) {
			logger.info("'IpAddress' is configured but not the 'macAddress', let's get IP address from the device...");
			
			int tcpPort= (wesConfig.getTcpPort()!=null)? wesConfig.getTcpPort().intValue(): WesConstants.DEFAULT_TCP_PORT; 
			String macAddress = WesUtils.getMac(wesConfig.getIpAddress(), tcpPort);
			wesConfig.setMacAddress(macAddress);
		}
		
		WesServer wesServer= null;
		if (wesConfig.getMacAddress()!=null) {
			boolean isValidMac= WesUtils.isValid_WES_MACAddress(wesConfig.getMacAddress());
			if (!isValidMac) {
				logger.warn("MAC Address is invalid! We are discarding the value ['{}']", wesConfig.getMacAddress());				
			}
			else {
				logger.error("MAC Address is ['{}']", wesConfig.getMacAddress());
				wesServer= new WesServer(wesConfig.getMacAddress(), wesConfig);
			}
		}
		
		if (wesServer==null) {
			logger.info("No 'IpAddress' is configured, let's run discovery mode...");		
			Map<String,String> macIpMap= discovery();
			if ((macIpMap!=null)&&(macIpMap.size()==1)) {
				String mac= macIpMap.keySet().iterator().next();
				String ipAddress= macIpMap.get(mac);
				wesConfig.setMacAddress(mac);
				wesConfig.setIpAddress(ipAddress);
				wesServer= new WesServer(wesConfig.getMacAddress(), wesConfig);
			}
		}
		if (wesServer!=null) {
			System.out.println(wesServer.getWesData());
			
			mqttWesClient.setWesServer(wesServer);
			
			mqttWesClient.start();
		}
		logger.info("End.");
	}

	
	private Map<String,String> discovery(){
		logger.info("Start scanning...");
		
		Map<String,String> scanResult= new WesClient().scan(wesConfig.getDiscoveryInterfaceIPs(), wesConfig.getTcpPort(), WesConstants.DEFAULT_HTTP_PORT);
		if ((scanResult!=null)&&(!scanResult.isEmpty())){
			String content = scanResult.entrySet()
	                .stream()
	                .map(e -> "Id:"+ e.getKey() + "->IP:" + e.getValue() + "")
	                .collect(Collectors.joining(", "));
	
			System.out.println("WES found: " + content);
		}
		else {
			System.out.println("No WES found!");
		}
		logger.info("End of scan.");
		return scanResult;
	}
}
