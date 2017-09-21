package com.netto.server.handler;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerJsonHandler extends SimpleChannelInboundHandler<byte[]> {
	private static Logger logger = Logger.getLogger(NettyServerJsonHandler.class);


    private NettoServiceChannelHandler channelHandler;
  

	public NettyServerJsonHandler(NettoServiceChannelHandler channelHandler) {

        this.channelHandler = channelHandler;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();	    
		logger.error("channel error",cause);

	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

        String body = new String(msg, "UTF-8");
        if(body.equals("")){
            return ;
        }
        this.channelHandler.received(ctx, body);
   
        return;
        
    }
       
        

}
