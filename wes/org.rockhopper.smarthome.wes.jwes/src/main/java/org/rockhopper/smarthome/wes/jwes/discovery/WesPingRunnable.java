/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.rockhopper.smarthome.wes.jwes.discovery;

import org.rockhopper.smarthome.wes.jwes.WesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This runnable pings the given IP address and is used by the {@see NetworkDiscoveryService}.
 * If the java ping does not work, a native ping will be tried. This procedure is necessary,
 * because in some OS versions (e.g. Windows 7) the java ping does not work reliably.
 *
 */
class WesPingRunnable implements Runnable {
    private Logger logger = LoggerFactory.getLogger(WesPingRunnable.class);
    
    private final String ip;
    private int tcpPort;
    private int httpPort;
    
    private final WesServerDiscoveryService service;

    public WesPingRunnable(String ip, Integer tcpPort, Integer httpPort, WesServerDiscoveryService service) {
        this.ip = ip;
        this.service = service;
        this.tcpPort= (tcpPort!=null)?tcpPort:WesConstants.DEFAULT_TCP_PORT;
        this.httpPort= (httpPort!=null)?httpPort:WesConstants.DEFAULT_HTTP_PORT;
        if (this.ip == null) {
            throw new RuntimeException("ip may not be null!");
        }
    }

    @Override
    public void run() {
        try {
        	logger.debug("WES Ping {}", ip);
            Boolean httpPingResult = WesUtils.httpPing(ip, httpPort);
            if (httpPingResult != null) {
                if (httpPingResult.booleanValue()) {
                    logger.info("Found WES Server IP=[{}]", ip);

                    String macAddress = WesUtils.getMac(ip, tcpPort);
                    if (macAddress != null) {
                        service.newServer(ip, macAddress); ///
                    } else {
                        logger.error("Fail to get MacAddress of WES Server with IP {}", ip);
                    }

                } else {
                    logger.debug("Network device {} is not a WES!", ip);
                }
            }
        } catch (Exception e) {
            logger.trace("Fail to ping '{}'", ip, e);
        }
        finally {
        	logger.debug("End of WES Ping {}", ip);
        }
    }
    
    public String getIp() {
		return ip;
    }
}
