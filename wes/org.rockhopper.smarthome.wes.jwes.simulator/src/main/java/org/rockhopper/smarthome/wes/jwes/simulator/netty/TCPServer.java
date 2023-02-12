/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rockhopper.smarthome.wes.jwes.simulator.netty;

import java.net.InetSocketAddress;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

/**
 * TCP Server
 *
 * @author Jibeom Jung
 */
@Component
public class TCPServer {
    @Autowired
    @Qualifier(value="tcpServerBootstrap")
    private final ServerBootstrap serverBootstrap;
    
    @Autowired
    @Qualifier(value="tcpSocketAddress")
    private final InetSocketAddress tcpPort;

    private Channel serverChannel;

    @Autowired
    public TCPServer(@Qualifier(value="tcpServerBootstrap") ServerBootstrap serverBootstrap, @Qualifier(value="tcpSocketAddress")InetSocketAddress tcpPort) {
		this.serverBootstrap= serverBootstrap;
		this.tcpPort= tcpPort;
	}
    
    public void start() throws Exception {
        serverChannel =  serverBootstrap.bind(tcpPort).sync().channel().closeFuture().sync().channel();
    }

    @PreDestroy
    public void stop() {
        if ( serverChannel != null ) {
            serverChannel.close();
            serverChannel.parent().close();
        }
    }
    
}
