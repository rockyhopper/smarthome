package org.rockhopper.smarthome.wes.wes2mqtt;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rockhopper.smarthome.wes.jwes.model.WesServer;
import org.rockhopper.smarthome.wes.jwes.model.data.WesOneWireRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCard;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Template;

public class HomeAssistantIntegration {
	
	public static final String HOMEASSISTANT_DISCOVERY_TOPIC_PREFIX= "homeassistant";
	
	
	private FreeMarkerConfigurer freeMarkerConfigurer; 
	private MqttPushClient mqttPushClient; 
	
	public HomeAssistantIntegration(MqttPushClient mqttPushClient, FreeMarkerConfigurer freeMarkerConfigurer) {
		this.freeMarkerConfigurer= freeMarkerConfigurer;
		this.mqttPushClient= mqttPushClient;
	}
	
	public void fulfillDiscovery(WesServer wesServer){
		
		if ((wesServer==null)||(wesServer.getWesData()==null)) {
			return;
		}
		
	
		registerWesRelaysSwitches(wesServer);
		registerWesRelaysCardsRelaySwitches(wesServer);
		
		/*
        String cmndTopic= mqttConfig.getBaseTopic() + "/" + mqttConfig.getCommandSubTopic() + "/";       
        cmndLabels.put(cmndTopic + labelToSubTopic(wesData.getRelay1().getValue().getLabel()), wesData.getRelay1().getValue());
        log.info("Adding cmdTopic '{}'", cmndTopic + labelToSubTopic(wesData.getRelay1().getValue().getLabel()));
        cmndLabels.put(cmndTopic + labelToSubTopic(wesData.getRelay2().getValue().getLabel()), wesData.getRelay2().getValue());        
        log.info("Adding cmdTopic '{}'", cmndTopic + labelToSubTopic(wesData.getRelay2().getValue().getLabel()));	
        */
	}
	
	private void registerWesRelaySwitch(WesServer wesServer, byte relayIdx) {
		try {
			Map<String,Object> values= new HashMap<String,Object>();
			values.put("wesServer", wesServer);
			WesRelay wesRelay= null;
			if (relayIdx==1) {
				wesRelay= wesServer.getWesData().getRelay1();
			}
			else if (relayIdx==2) {
				wesRelay= wesServer.getWesData().getRelay2();
			}
			else {
				return;
			}			
			values.put("wesRelay", wesRelay);	
			String payload= generateJsonByTemplate("relay", values);			
			mqttPushClient.publishToTopic(0, true, String.format("%s/switch/wes_%s/wes_relay%d/config", HOMEASSISTANT_DISCOVERY_TOPIC_PREFIX, wesServer.getMacAddress(), wesRelay.getIndex()), payload);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void registerWesRelaysSwitches(WesServer wesServer) {
		registerWesRelaySwitch(wesServer, (byte)1);
		registerWesRelaySwitch(wesServer, (byte)2);
	}
	
	private void registerWesRelaysCardsRelaySwitches(WesServer wesServer) {
		try {
			if ((wesServer.getWesData().getRelaysCards()!=null) &&
			    (wesServer.getWesData().getRelaysCards().getCards()!=null)
			    ){
				List<WesRelaysCard> wesRelaysCards= wesServer.getWesData().getRelaysCards().getCards();
				for (WesRelaysCard wesRelaysCard : wesRelaysCards) {
					if (wesRelaysCard!=null) {
						Map<String,Object> values= new HashMap<String,Object>();
						values.put("wesRelaysCard", wesRelaysCard);
						List<WesOneWireRelay> oneWireRelays= wesRelaysCard.getRelays();
						if (oneWireRelays!=null) {
							for (WesOneWireRelay oneWireRelay: oneWireRelays) {
								if (oneWireRelay!=null) {
									values.put("oneWireRelay", oneWireRelay);	
									values.put("oneWireRelayIdxMinusOne", oneWireRelay.getIndex()-1);
									String payload= generateJsonByTemplate("relaysCardsRelay", values);			
									mqttPushClient.publishToTopic(0, true, String.format("%s/switch/wes_%s/wes_relay1%d%d/config", HOMEASSISTANT_DISCOVERY_TOPIC_PREFIX,wesServer.getMacAddress(), wesRelaysCard.getIndex(), oneWireRelay.getIndex()), payload);
								}
							}
						}					
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private String generateJsonByTemplate(String templateName, Map<String, Object> input) throws Exception{
        String sourceCode = null;
        try{
        	Template template= freeMarkerConfigurer.getConfiguration().getTemplate(templateName + ".ftl");
            StringWriter writer = new StringWriter();

            template.process(input, writer);
            sourceCode = writer.toString();
        } 
        catch (Exception exception){
            throw new Exception("Processing failed for template '" + templateName  + "' with error: " + exception.getMessage(), exception);
        }

        return sourceCode;
    }
	
	
	
}
