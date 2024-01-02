package org.rockhopper.smarhome.hsb.server;

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
    private MqttHsbClient mqttHsbClient;
    
    public static void main(String[] args) {
        SpringApplication.run(MqttApplication.class, args);
    }

	@EventListener(ApplicationReadyEvent.class)
	public void afterSpringStartup() {
		logger.info("Start...");
		mqttHsbClient.start();
		
		logger.info("End.");
	}
}
