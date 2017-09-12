package com.netto.server.handler;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

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
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractServiceChannelHandler implements NettoServiceChannelHandler{
    protected static Gson gson = new Gson();
    protected static Logger logger = Logger.getLogger(SynchronousChannelHandler.class);
    
    private Map<String, NettoServiceBean> serviceBeans;
    private List<InvokeMethodFilter> filters;

    
    public AbstractServiceChannelHandler(Map<String, NettoServiceBean> serviceBeans,  List<InvokeMethodFilter> filters) {
        this.serviceBeans = serviceBeans;
        ServiceBean bean = new ServiceBean();
        bean.setRef("$serviceDesc");
        NettoServiceBean serivceBean = new NettoServiceBean(bean, new ServiceDescApiImpl(this.serviceBeans));
        this.serviceBeans.put("$serviceDesc", serivceBean);
        this.filters = filters;

    }    
       

    public void handle(ChannelHandlerContext ctx, String message) throws Exception {
        ServiceResponse resObj = new ServiceResponse();
        try{
            ServiceRequest reqObj = gson.fromJson(message, ServiceRequest.class);
            if (serviceBeans.containsKey(reqObj.getServiceName())) {
                ServiceProxy proxy = new ServiceProxy(reqObj, serviceBeans.get(reqObj.getServiceName()),
                        filters);
    
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
        }
        catch(Throwable t){
            logger.error("error when process request "+message,t);
        }
        String response = gson.toJson(resObj);
        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        encoded.writeBytes(response.getBytes());
        encoded.writeCharSequence(Constants.PROTOCOL_REQUEST_DELIMITER, Charset.defaultCharset());
        ctx.write(encoded);
        ctx.flush();
        
    }

}
