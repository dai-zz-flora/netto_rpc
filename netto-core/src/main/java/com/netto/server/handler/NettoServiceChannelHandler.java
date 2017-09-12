package com.netto.server.handler;

import io.netty.channel.ChannelHandlerContext;

public interface NettoServiceChannelHandler {

    /**
     * on message received.
     * 
     * @param channel channel.
     * @param message message.
     */
    void received(ChannelHandlerContext ctx, String message) throws Exception;
}
