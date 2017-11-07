package com.netto.client.transport;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.netto.client.provider.ServiceProvider;
import com.netto.core.context.RpcContext;
import com.netto.core.exception.RemoteAccessException;
import com.netto.core.message.NettoFrame;

public class HttpTransport implements Transport{

    private int timeout;
    private ServiceProvider externalProvider;

    private static Logger logger =  Logger.getLogger(HttpTransport.class);
    
    private  HttpClient httpClient;


    public HttpTransport(ServiceProvider provider,int timeout,HttpClient httpClient){
        this.externalProvider = provider;
        this.timeout = timeout;
        this.httpClient = httpClient;
    }
  
    
    private String getInvokeUrl(NettoFrame frame) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(externalProvider.getRouteConfig().getTarget());
        if (!externalProvider.getRouteConfig().getTarget().endsWith("/")) {
            sb.append("/");
        }
        sb.append(externalProvider.getServerDesc().getServerApp()).append("/").append(frame.getServiceName())
                .append("/").append(frame.getMethodName());
        return sb.toString();
    }    

    @Override
    public NettoFrame request(NettoFrame requestFrame) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
      
        try {
            // 创建httppost
            HttpPost post = new HttpPost(this.getInvokeUrl(requestFrame));
            post.setConfig(requestConfig);
            post.addHeader("$app", externalProvider.getServerDesc().getServerApp());
            if (RpcContext.getRouterContext() != null) {
                post.addHeader("$router", RpcContext.getRouterContext());
            }

            if (requestFrame.getArgsLen() != -1) {
                post.addHeader(NettoFrame.ARGSLEN_HEADER, String.valueOf(requestFrame.getArgsLen()));
            } else {
                post.addHeader(NettoFrame.ARGSLEN_HEADER, "0");
            }
            if (requestFrame.getSignature()!=null) {

                post.addHeader(NettoFrame.SIGNATURE_HEADER, requestFrame.getSignature());
            }

            ByteArrayEntity se = new ByteArrayEntity(requestFrame.getBody(), ContentType.APPLICATION_JSON);
            post.setEntity(se);

            HttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
   
            if (response.getStatusLine().getStatusCode() == 200) {
                NettoFrame responseFrame = new NettoFrame();
                responseFrame.setBody(EntityUtils.toByteArray(entity));
                
                return responseFrame;

            } else {
                String body = EntityUtils.toString(entity, "UTF-8");
                throw new RemoteAccessException(body);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new RemoteAccessException(t.getMessage(),t);
        } finally {
            
        }
    }

  

}
