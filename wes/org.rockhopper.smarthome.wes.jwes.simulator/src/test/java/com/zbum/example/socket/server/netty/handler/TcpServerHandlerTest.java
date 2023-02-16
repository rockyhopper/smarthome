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
package com.zbum.example.socket.server.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rockhopper.smarthome.wes.jwes.simulator.netty.handler.TcpServerHandler;

import java.net.SocketAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for  SomethingServerHandler.java
 *
 * @author Jibeom Jung
 */
public class TcpServerHandlerTest {

    private TcpServerHandler tcpServerHandler;

    private ChannelHandlerContext channelHandlerContext;

    private Channel channel;

    private SocketAddress remoteAddress;

    @Before
    public void setUp() {
    	tcpServerHandler = new TcpServerHandler();

        channelHandlerContext = mock(ChannelHandlerContext.class);
        channel = mock(Channel.class);
        remoteAddress = mock(SocketAddress.class);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testChannelActive() throws Exception {
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(channelHandlerContext.channel().remoteAddress()).thenReturn(remoteAddress);
        tcpServerHandler.channelActive(channelHandlerContext);
    }

    @Test
    public void testChannelRead() throws Exception {
        when(channelHandlerContext.channel()).thenReturn(channel);
        tcpServerHandler.channelRead(channelHandlerContext, "test message");
    }

    @Test
    public void testExceptionCaught() throws Exception {

    }
}