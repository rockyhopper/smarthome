package org.rockhopper.smarthome.wes.jwes.simulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {
	
    private int tcpPort;

    private int bossCount;

    private int workerCount;

    private boolean keepAlive;

    private int backlog;
    
    public int getTcpPort() {
		return tcpPort;
	}
    
    public int getBossCount() {
		return bossCount;
	}
    
    public int getWorkerCount() {
		return workerCount;
	}
    
    public int getBacklog() {
		return backlog;
	}
    
    public boolean isKeepAlive() {
		return keepAlive;
	}
    
	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public void setBossCount(int bossCount) {
		this.bossCount = bossCount;
	}

	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}
}
