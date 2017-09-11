package com.netto.server.handler;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.netto.context.ServiceRequest;
import com.netto.context.ServiceResponse;
import com.netto.filter.InvokeMethodFilter;
import com.netto.server.desc.impl.ServiceDescApiImpl;
import com.netto.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerJsonHandler extends SimpleChannelInboundHandler<byte[]> {
	private static Logger logger = Logger.getLogger(NettyServerJsonHandler.class);
	private static Gson gson = new Gson();
	private Map<String, Object> serviceBeans;
	private List<InvokeMethodFilter> filters;

	public NettyServerJsonHandler(Map<String, Object> serviceBeans, List<InvokeMethodFilter> filters) {
		this.serviceBeans = serviceBeans;
		this.serviceBeans.put("$serviceDesc", new ServiceDescApiImpl(this.serviceBeans));
		this.filters = filters;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) msg;
			byte[] req = new byte[buf.readableBytes()];
			buf.readBytes(req);
			String body = new String(req, "UTF-8");
			ServiceRequest reqObj = gson.fromJson(body, ServiceRequest.class);
			ServiceResponse resObj = new ServiceResponse();
			if (this.serviceBeans.containsKey(reqObj.getServiceName())) {
				ServiceProxy proxy = new ServiceProxy(reqObj, this.serviceBeans.get(reqObj.getServiceName()),
						this.filters);

				try {
					resObj.setSuccess(true);
					resObj.setBody(proxy.callService());
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
					resObj.setSuccess(false);
					resObj.setBody(t.getMessage());
				}
			} else {
				resObj.setSuccess(false);
				resObj.setBody("service " + reqObj.getServiceName() + " is not exsist!");
			}
			String response = gson.toJson(resObj) ;
			ByteBuf encoded = ctx.alloc().buffer(4 * response.length()+8);
			encoded.writeBytes(response.getBytes());
			encoded.writeBytes(Constants.PROTOCOL_REQUEST_DELIMITER.getBytes());
//			encoded.writeBytes(Constants.PROTOCOL_REQUEST_DELIMITER_BYTE_BUF);
			ctx.write(encoded);
			ctx.flush();
			return;
		}
		super.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		ctx.close();
	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

        String body = new String(msg, "UTF-8");
        ServiceRequest reqObj = gson.fromJson(body, ServiceRequest.class);
        ServiceResponse resObj = new ServiceResponse();
        if (this.serviceBeans.containsKey(reqObj.getServiceName())) {
            ServiceProxy proxy = new ServiceProxy(reqObj, this.serviceBeans.get(reqObj.getServiceName()),
                    this.filters);

            try {
                resObj.setSuccess(true);
                resObj.setBody(proxy.callService());
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                resObj.setSuccess(false);
                resObj.setBody(t.getMessage());
            }
        } else {
            resObj.setSuccess(false);
            resObj.setBody("service " + reqObj.getServiceName() + " is not exsist!");
        }
        String response = gson.toJson(resObj) + "\r\n";
        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        encoded.writeBytes(response.getBytes());
        ctx.write(encoded);
        ctx.flush();
        return;
        
    }

}
