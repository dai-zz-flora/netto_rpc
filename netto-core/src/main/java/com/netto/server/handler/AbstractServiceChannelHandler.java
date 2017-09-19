package com.netto.server.handler;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.netto.context.ServiceRequest;
import com.netto.context.ServiceResponse;
import com.netto.filter.InvokeMethodFilter;
import com.netto.server.bean.NettoServiceBean;
import com.netto.server.bean.ServiceBean;
import com.netto.server.desc.impl.ServiceDescApiImpl;
import com.netto.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractServiceChannelHandler implements NettoServiceChannelHandler {
    protected static Gson gson = new Gson();
    protected static Logger logger = Logger.getLogger(SynchronousChannelHandler.class);

    private Map<String, NettoServiceBean> serviceBeans;
    private List<InvokeMethodFilter> filters;

    private long reponseWriteTimeout = 10;// 3 seconds;

    public AbstractServiceChannelHandler(Map<String, NettoServiceBean> serviceBeans, List<InvokeMethodFilter> filters) {
        this.serviceBeans = serviceBeans;
        ServiceBean bean = new ServiceBean();
        bean.setRef("$serviceDesc");
        NettoServiceBean serivceBean = new NettoServiceBean(bean, new ServiceDescApiImpl(this.serviceBeans));
        this.serviceBeans.put("$serviceDesc", serivceBean);
        this.filters = filters;

    }


    public void sendResponse(ChannelHandlerContext ctx, ServiceResponse resObj) {
        StringWriter writer =new StringWriter();
        gson.toJson(resObj,writer);
        writer.write(Constants.PROTOCOL_REQUEST_DELIMITER);
//        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
//        encoded.writeBytes(response.getBytes());
//        encoded.writeCharSequence(Constants.PROTOCOL_REQUEST_DELIMITER, Charset.defaultCharset());
//        ChannelFuture future = ctx.write(encoded);
//        ctx.flush();
        ChannelFuture future = ctx.writeAndFlush(writer.toString());
        

        try {
            boolean success = future.await(this.reponseWriteTimeout, TimeUnit.SECONDS);

            if (future.cause() != null) {
                throw new RemoteAccessException("error response error ", future.cause());
            } else if (!success) {
                throw new RemoteAccessException("response error timeout");
            }
        } catch (InterruptedException e) {
            throw new RemoteAccessException("awit Interrupted", e);
        }
        finally{
        
        }
    }

    public void handle(ChannelHandlerContext ctx, String message) throws Exception {
        ServiceResponse resObj = new ServiceResponse();
        try {
            resObj.setSuccess(false);
            ServiceRequest reqObj = gson.fromJson(message, ServiceRequest.class);
            if (serviceBeans.containsKey(reqObj.getServiceName())) {
                ServiceProxy proxy = new ServiceProxy(reqObj, serviceBeans.get(reqObj.getServiceName()), filters);

                try {

                    resObj.setBody(proxy.callService());
                    resObj.setSuccess(true);

                } catch (Throwable t) {

                    logger.error(t.getMessage(), t);
                    resObj.setBody(t.getMessage());
                }
                this.sendResponse(ctx, resObj);

            } else {
                resObj.setBody("service " + reqObj.getServiceName() + " is not exsist!");
                this.sendResponse(ctx, resObj);
            }
        } catch (RemoteAccessException e) {
            logger.error("error when reply request " + message, e);
        } catch (Throwable t) {
            logger.error("error when process request " + message, t);
            resObj.setBody("error when process request " + message);
            this.sendResponse(ctx, resObj);
        }

    }

}
