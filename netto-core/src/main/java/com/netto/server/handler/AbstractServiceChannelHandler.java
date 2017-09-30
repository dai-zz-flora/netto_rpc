package com.netto.server.handler;

import java.io.IOException;
import java.io.StringWriter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.netto.context.ServiceRequest;
import com.netto.context.ServiceResponse;

import com.netto.filter.InvokeMethodFilter;
import com.netto.server.bean.NettoServiceBean;
import com.netto.server.bean.ServiceBean;
import com.netto.server.desc.impl.ServiceDescApiImpl;
import com.netto.server.exception.NettoIOException;
import com.netto.server.exception.RemoteAccessException;
import com.netto.util.Constants;
import com.netto.util.RandomStrGenerator;
import com.netto.util.SignatureVerifier;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractServiceChannelHandler implements NettoServiceChannelHandler {

    protected static Logger logger = Logger.getLogger(AbstractServiceChannelHandler.class);

    private Map<String, NettoServiceBean> serviceBeans;
    private List<InvokeMethodFilter> filters;

    private long reponseWriteTimeout = 10;// 3 seconds;
    


    private ObjectMapper objectMapper;

    @Override
    public void caught(ChannelHandlerContext ctx, Throwable cause) {
        try{
            ServiceResponse<String> resObj = new  ServiceResponse<String>();
            resObj.setErrorMessage(cause.getMessage());
            StringWriter writer = new StringWriter();
            this.objectMapper.writeValue(writer, resObj);
            writer.write(Constants.PROTOCOL_REQUEST_DELIMITER);
            
            ctx.writeAndFlush(writer.toString());
        }
        catch(Throwable t){
            logger.error("caught error failed ", t);
        }
        
    }

    public AbstractServiceChannelHandler(Map<String, NettoServiceBean> serviceBeans, List<InvokeMethodFilter> filters) {
        this.serviceBeans = serviceBeans;
        ServiceBean bean = new ServiceBean();
        bean.setRef("$serviceDesc");
        NettoServiceBean serivceBean = new NettoServiceBean(bean, new ServiceDescApiImpl(this.serviceBeans));
        this.serviceBeans.put("$serviceDesc", serivceBean);

        this.filters = filters;
        this.initJson();
    }

    private void initJson() {
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
        
        ServiceRequestJacksonDeserializer deserializer = new ServiceRequestJacksonDeserializer(ServiceRequest.class,objectMapper);
        for (String key : serviceBeans.keySet()) {
            NettoServiceBean bean = serviceBeans.get(key);
            Class clazz = bean.getObject().getClass();
            deserializer.registerMethodParameterTypes(key, clazz);
        }
        
        SimpleModule simpleModule  = new SimpleModule();
        
        simpleModule.addDeserializer(ServiceRequest.class,deserializer);
        objectMapper.registerModule(simpleModule);



    }

    public void sendResponse(ChannelHandlerContext ctx, ServiceResponse resObj) throws JsonGenerationException, JsonMappingException, IOException {
        StringWriter writer = new StringWriter();
        this.objectMapper.writeValue(writer, resObj);
        writer.write(Constants.PROTOCOL_REQUEST_DELIMITER);

        ChannelFuture future = ctx.writeAndFlush(writer.toString());

        try {
            boolean success = future.await(this.reponseWriteTimeout, TimeUnit.SECONDS);

            if (future.cause() != null) {
                throw new NettoIOException("error response error ", future.cause());
            } else if (!success) {
                throw new NettoIOException("response error timeout");
            }
        } catch (InterruptedException e) {
            throw new RemoteAccessException("await Interrupted", e);
        } finally {

        }
    }

    private boolean verifySignature(NettoMessage message) throws Exception {
        Map<String, String> headers = message.getHeaders();

        String signatureStr = headers.get(NettoFrame.SIGNATURE_HEADER);

        if (signatureStr != null) {
            String[] signatureTokens = signatureStr.split(",");
            
            if(signatureTokens.length!=2){
                
                logger.error("verified error,"+signatureStr);
                return false;
            }
            
            String signature = signatureTokens[0];
            int saltIndex = Integer.parseInt(signatureTokens[1]);// salt序号

            /* 根据时间戳来解析签名 */
            String realSignature = RandomStrGenerator.extractRandomStrBasedTimestamp(signature,
                    RandomStrGenerator.HOUR_TIMESTAMP);

            boolean verified = SignatureVerifier.verify(message.getBody(), saltIndex, realSignature);
            
            if(!verified){
                logger.error("verified error,"+signatureStr);
            }
            return verified;

        }

        return true;
    }

    public void handle(ChannelHandlerContext ctx, NettoMessage message) throws Exception {
        ServiceResponse<Object> resObj = new ServiceResponse<Object>();
        
        try {
            resObj.setSuccess(false);

            boolean verified = this.verifySignature(message);
            if (verified) {

                ServiceRequest reqObj = objectMapper.readValue(new String(message.getBody(), "utf-8"), ServiceRequest.class);
                if (serviceBeans.containsKey(reqObj.getServiceName())) {
                    ServiceProxy proxy = new ServiceProxy(reqObj, serviceBeans.get(reqObj.getServiceName()), filters);

                    try {
                        Object ret = proxy.callService();
                        //resObj.setBody(objectMapper.writeValueAsString(ret));
                        resObj.setRetObject(ret);
                        resObj.setSuccess(true);

                    } catch (Throwable t) {

                        logger.error(t.getMessage(), t);
                        resObj.setErrorMessage(t.getMessage());
                    }
                    this.sendResponse(ctx, resObj);

                } else {
                    resObj.setErrorMessage("service " + reqObj.getServiceName() + " is not exsist!");
                    this.sendResponse(ctx, resObj);
                }
            } else {
                resObj.setErrorMessage("service  is not verified!");
                this.sendResponse(ctx, resObj);
            }
            
        } catch (RemoteAccessException re) {
            logger.error("error when reply request " + message, re);
        } catch(NettoIOException ne){
            logger.error("io error when reply request " + message, ne);
        }        
        catch (Throwable t) {
            logger.error("error when process request " + message, t);
            resObj.setErrorMessage("error when process request " + message);
            this.sendResponse(ctx, resObj);
        }

    }
    
    public void setReponseWriteTimeout(long reponseWriteTimeout) {
        this.reponseWriteTimeout = reponseWriteTimeout;
    }

}
