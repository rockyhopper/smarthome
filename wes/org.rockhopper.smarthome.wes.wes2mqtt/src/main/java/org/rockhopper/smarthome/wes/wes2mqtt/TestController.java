package org.rockhopper.smarthome.wes.wes2mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author bam
 * 2020 March 5th 2013
 * TestController.java
 *
 */
@RestController
@RequestMapping("/")
public class TestController {


	@Value("${gretings.suffix:default}")
	private String suffixGreetingsMsg;


	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot! " + suffixGreetingsMsg;
	}

	
    @Autowired
    private MqttPushClient mqttPushClient;

    @GetMapping(value = "/publishTopic")
    public String publishTopic() {
    	String topicString = "test";
        try {
            mqttPushClient.publishToSubTopic(0, false, topicString, "Test posting");
            return "ok";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
 // Send custom message content (using default theme)
    @RequestMapping("/publishTopic/{data}")
    public void test1(@PathVariable("data") String data) {
    	String topicString = "test";
    	try {
    	    mqttPushClient.publishToSubTopic(0,false,topicString, data);
    	} catch (Exception e) {
    	    // Log error but don't expose to client
    	    System.err.println("MQTT publish error: " + e.getMessage());
    	}
    }
 
    // Send custom message content and specify subject
    @RequestMapping("/publishTopic/{topic}/{data}")
    public void test2(@PathVariable("topic") String topic, @PathVariable("data") String data) {
    	try {
    	    mqttPushClient.publishToSubTopic(0,false,topic, data);
    	} catch (Exception e) {
    	    // Log error but don't expose to client
    	    System.err.println("MQTT publish error: " + e.getMessage());
    	}
    }
}