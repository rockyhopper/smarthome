package org.rockhopper.smarthome.wes.jwes.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.WesConstants;
import org.rockhopper.smarthome.wes.jwes.communicator.FtpClient;
import org.rockhopper.smarthome.wes.jwes.communicator.WesHttpClient;
import org.rockhopper.smarthome.wes.jwes.communicator.tcp.TcpPolling;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.XmlHandler;
import org.rockhopper.smarthome.wes.jwes.discovery.WesServerDiscoveryService;
import org.rockhopper.smarthome.wes.jwes.discovery.WesUtils;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCards;
import org.rockhopper.smarthome.wes.jwes.model.data.WesSensor;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesLabelsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesServer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String macAddress;

    private WesConfig wesConfig;

    private WesData wesData;
    
    private XmlHandler xmlHandler;
    
    private TcpPolling tcpPolling;

    /**
     * Instiantiate and initialize a WES based on its MAC Address
     *
     * @param macAddress MAC Address can be xx:xx:xx:xx:xx:xx (else xxxxxxxxxxxx)
     */
    public WesServer(String macAddress) {
       this(macAddress, null);
    }

    public WesServer(String macAddress, WesConfig wesConfig) {
    	this.wesConfig = wesConfig;
    	
    	setMacAddress(macAddress);        

    	if ((wesConfig!=null)&&(wesConfig.getIpAddress()!=null)){
    		initialize();
    	}
    	else {
    		initialize(macAddress);
    	}
    }

    private void initialize(String macAddress) {
        WesServerDiscoveryService wesServerDiscoveryService = new WesServerDiscoveryService() {
            @Override
            public void newServer(String newIp, String newMacAddress) {
                logger.info("Found WES Server with MAC address [{}], the IP Address is [{}]", newMacAddress, newIp);
                if (macAddress.equals(newMacAddress)) {
                    logger.info("We have found the WES Server we were looking: MAC address [{}], IP Address is [{}]",
                            macAddress, newIp);
                    if (wesConfig == null) {
                        wesConfig = new WesConfig(newIp);                        
                    } else {
                        wesConfig.setIpAddress(newIp);
                    }
                    interruptScan();
                }
            }
        };
        if (wesConfig!=null) {
	        wesServerDiscoveryService.setDiscoveryInterfaceIPs(wesConfig.getDiscoveryInterfaceIPs());
	        wesServerDiscoveryService.setTcpPort(wesConfig.getTcpPort());
        }
        wesServerDiscoveryService.setHttpPort(WesConstants.DEFAULT_HTTP_PORT);        
        wesServerDiscoveryService.scan();       
        
        if ((wesConfig != null) && (wesConfig.getIpAddress() != null)) {
            initialize(true);
        }
    };

    private void initialize() {
        initialize(false);
    }

    private void initialize(boolean skipMacAddressCheck) {
        if ((wesConfig == null) || (wesConfig.getIpAddress() == null)) {
            throw new UnsupportedOperationException("Cannot initialize WES with IP Address");
        }
        logger.info("About to initialize WesServer[{}] using IP Address[{}]", macAddress, wesConfig.getIpAddress());

        if (!skipMacAddressCheck) {
            if (!checkMac()) {
                logger.warn(
                        "The MAC address for IP Address is not matching the expected WES MAC Address. Fallback to MAC Address initialization.");
                initialize(macAddress);
            } else {
                logger.debug("MAC address [{}] is checked!", macAddress);
            }
        }

        initPhase1();
    }

    private void initPhase1() {
        wesData = WesData.getInstance(WesSensor.MAX_SENSORS, WesRelaysCards.MAX_RELAYSCARD);
               
        WesLabelsHelper.label(wesData);
        
        xmlHandler= new XmlHandler(wesData);
        try (InputStream cgx= IOUtils.toInputStream(xmlHandler.buildCGX(), "ISO-8859-1")){
        	uploadFile(cgx, "PHASE1.CGX");	  
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        wesData = (WesData) xmlHandler.deserialize(download("PHASE1.CGX", wesConfig.getHttpUser(), wesConfig.getHttpPass()));
        
        xmlHandler= new XmlHandler(wesData);
    }

    private boolean checkMac() {
        String netWorkMACAddress = WesUtils.getMac(wesConfig.getIpAddress(), wesConfig.getTcpPort());
        if (netWorkMACAddress == null) {
            throw new RuntimeException("Fail to check MAC Address for WES[" + macAddress + "] (using IP ["
                    + wesConfig.getIpAddress() + "])");
        }
        return netWorkMACAddress.equals(macAddress) ? true : false;
    }

    

    private void uploadFile(InputStream localStreamToUpload, String remoteFileName) {
        FtpClient ftpClient = null;
        try {
            ftpClient = new FtpClient(getWesConfig().getIpAddress());

            boolean logged = ftpClient.login(getWesConfig().getFtpUser(), getWesConfig().getFtpPass());
            if (logged) {
                try (BufferedInputStream bis = new BufferedInputStream(localStreamToUpload)) {
                    ftpClient.changeWorkingDirectory("/JWES");
                    ftpClient.uploadFile(remoteFileName, bis);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftpClient != null) {
                ftpClient.logout();
            }
        }
    }

    private String download(String remoteFileName, String httpUser, String httpPass) {
        WesHttpClient httpClient = new WesHttpClient(wesConfig.getIpAddress(), WesConstants.DEFAULT_HTTP_PORT);
        httpClient.login(httpUser, httpPass);
        return httpClient.httpGet("/JWES/" + remoteFileName);
    }

    public Set<Field<?, ?>> label() {
        if ((wesData!=null)&&(!wesData.isTruncated())){
        	wesData.truncate();	
            return WesLabelsHelper.label(wesData);
        }
        else {
        	return null;
        }
    }
    
    public boolean startPolling(WesEventListener eventListener){
        if ((wesData!=null)&&(!wesData.isTruncated())){
        	wesData.truncate();	
        }
        
    	tcpPolling= new TcpPolling(wesConfig.getIpAddress(), wesConfig.getTcpPort(), wesData);
    	tcpPolling.setEventListener(eventListener);
    	tcpPolling.run();
    	return true;
    }
    
    public void stopPolling(){
    	if (tcpPolling!=null) {
    		tcpPolling.halt();
    	}
    }
    
    public WesConfig getWesConfig() {
        return wesConfig;
    }

    public WesData getWesData() {
        return wesData;
    }

    public void setMacAddress(String macAddress) {
        if (macAddress != null) {
            boolean isValid = false;
            String rawMacAddress = StringUtils.remove(macAddress, ":");
            if (rawMacAddress.length() == 12) {
                try {
                    Base64.getDecoder().decode(rawMacAddress);
                    isValid = true;
                } catch (IllegalArgumentException iae) {
                    logger.error("Fail to parse WES MAC Address []", rawMacAddress, iae);
                }
            }
            if (!isValid) {
                logger.error("Given WES MAC Address is invalid []", macAddress);
            }
        }
        this.macAddress = macAddress;
    }
	
    public String getMacAddress() {
		return macAddress;
	}
    
    public <V, W> void forceUpdate(Field<V, W> field, V newValue) {
    	if (tcpPolling!=null) {
			tcpPolling.forceUpdate(field, newValue);
		}
    }
}
