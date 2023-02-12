package org.rockhopper.smarthome.wes.jwes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.rockhopper.smarthome.wes.jwes.discovery.WesServerDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesClient {
	static Logger log= LoggerFactory.getLogger(WesClient.class);
	
	public Map<String,String> scan(String discoveryInterfaceIPs){
		return scan(discoveryInterfaceIPs, WesConstants.DEFAULT_TCP_PORT, WesConstants.DEFAULT_HTTP_PORT);
	}
	
	public Map<String,String> scan(String discoveryInterfaceIPs, Integer tcpPort, Integer httpPort){
		final Map<String,String> foundWES= new HashMap<String,String>();
        WesServerDiscoveryService wesServerDiscoveryService = new WesServerDiscoveryService() {
            @Override
            public void newServer(String newIp, String newMacAddress) {
                log.info("Found WES Server with MAC address [{}], the IP Address is [{}]", newMacAddress, newIp);
                foundWES.put(newMacAddress, newIp);
            }            
        };        
        wesServerDiscoveryService.setDiscoveryInterfaceIPs(discoveryInterfaceIPs);
        wesServerDiscoveryService.setTcpPort(tcpPort);
        wesServerDiscoveryService.setHttpPort(httpPort);
        wesServerDiscoveryService.scan();
        return foundWES;
	}
	
	
	public static void main(String[] args) {
		System.out.println(String.format("Command-line Args[DiscoveryInterfaceIPs]: %s", Arrays.toString(args)));
		
		log.info("Start scanning...");
		
		Map<String,String> scanResult= new WesClient().scan(((args.length>0)?args[0]:null));
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
		log.info("End of scan.");
	}
}
