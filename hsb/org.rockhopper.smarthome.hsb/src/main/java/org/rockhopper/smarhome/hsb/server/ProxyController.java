package org.rockhopper.smarhome.hsb.server;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * 
 * @author bam
 * 2020 March 5th 2013
 * TestController.java
 *
 */
@RestController
@RequestMapping("/")
public class ProxyController {
    
	private final Logger logger= LoggerFactory.getLogger(getClass());
    
    @Autowired
    private HaConfig haConfig;
	
    @Autowired
    private RestClient haRestClient;
	
	@Value("${gretings.suffix:default}")
	private String suffixGreetingsMsg;


	@GetMapping("/")
	public String index() {
		return "Greetings from HSB! " + suffixGreetingsMsg;
	}
    
    @GetMapping(value = "/proxycmd/{key}")
    public String publishTopic(@PathVariable("key") String key) {
    	boolean success= false;
    	switch (key) {
    		case "momentary_parking_somfy_reflex":
    			success= switchProxiedCommand(key);
    			break;
    		default:
    			logger.info("No proxied command named '{}'", key);    	
    			break;
    	}
        return success?"Success!":"Failure!";
    }
    
    private boolean switchProxiedCommand(String switchEntityId){
		ResponseEntity<Void>  response= haRestClient.post().uri("%s://%s:%s/api/services/switch/turn_on".formatted(haConfig.getProtocol(),haConfig.getHost(),haConfig.getPort()))
													.body("{\"entity_id\": \"switch.%s\"}".formatted(switchEntityId))
													.retrieve()
													.toBodilessEntity();
		int responseCode= response.getStatusCode().value();
		return (responseCode==200);
    }
    
}