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
package org.rockhopper.smarthome.wes.jwes.simulator.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

/**
 * event handler to process receiving messages
 *
 * @author Jibeom Jung
 */
@Component
@ChannelHandler.Sharable
public class TcpServerHandler extends ChannelInboundHandlerAdapter {
	private Logger log= LoggerFactory.getLogger(getClass().getName());
		
	public TcpServerHandler() {
	}
	
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
        if (log.isDebugEnabled()) {
            log.debug(ctx.channel().remoteAddress() + "");
        }
        String channelKey = ctx.channel().remoteAddress().toString();
        
        log.info("Channel key is " + channelKey + "\r\n");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String stringMessage = (String) msg;
        if (log.isDebugEnabled()) {
            log.debug(stringMessage);
        }

        if ("gI000".equals(stringMessage)) {
        	// Get Mac Address
        	ctx.channel().writeAndFlush("0011223344AA" + "\n\r");
        }
        else {
        	ctx.channel().writeAndFlush("???" + "\n\r");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	if (cause instanceof ReadTimeoutException) {
            if (log.isDebugEnabled()) {
                log.debug(ctx.channel().remoteAddress() + " has been disconnected due to inactivity!");
            }
        }
        else {
            log.error(cause.getMessage(), cause);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        Assert.notNull(ctx, "[Assertion failed] - ChannelHandlerContext is required; it must not be null");

        // String channelKey = ctx.channel().remoteAddress().toString();
    }
}
