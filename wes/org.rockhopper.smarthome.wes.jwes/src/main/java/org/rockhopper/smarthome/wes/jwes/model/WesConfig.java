package org.rockhopper.smarthome.wes.jwes.model;

import org.rockhopper.smarthome.wes.jwes.WesConstants;

public class WesConfig {


    // The IP or host name of the WES device
    private String ipAddress;

    // Port of the WES to communicate with (TCP)
    private Integer tcpPort = WesConstants.DEFAULT_TCP_PORT;

    // User's identifier for HTTP Access
    private String httpUser = WesConstants.DEFAULT_HTTP_USER;

    // User's password for HTTP Access
    private String httpPass = WesConstants.DEFAULT_HTTP_PASS;

    // User's identifier for FTP Access
    private String ftpUser = WesConstants.DEFAULT_FTP_USER;

    // User's password for FTP Access
    private String ftpPass = WesConstants.DEFAULT_FTP_PASS;

    // List of Network Interfaces on which discovery must be performed (optional)
    private String discoveryInterfaceIPs;
    
    public WesConfig(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getHttpUser() {
        return httpUser;
    }

    public void setHttpUser(String httpUser) {
        this.httpUser = httpUser;
    }

    public String getHttpPass() {
        return httpPass;
    }

    public void setHttpPass(String httpPass) {
        this.httpPass = httpPass;
    }

    public String getFtpUser() {
        return ftpUser;
    }

    public void setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
    }

    public String getFtpPass() {
        return ftpPass;
    }

    public void setFtpPass(String ftpPass) {
        this.ftpPass = ftpPass;
    }
    
    public void setDiscoveryInterfaceIPs(String discoveryInterfaceIPs) {
		this.discoveryInterfaceIPs = discoveryInterfaceIPs;
	}
    
    public String getDiscoveryInterfaceIPs() {
		return discoveryInterfaceIPs;
	}
}
