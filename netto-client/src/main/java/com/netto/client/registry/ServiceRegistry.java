package com.netto.client.registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netto.client.http.HttpClientBuilder;
import com.netto.client.util.NamedThreadFactory;
import com.netto.core.context.RouteConfig;
import com.netto.core.context.ServerAddressGroup;
import com.netto.core.util.Constants;
import com.netto.core.util.JsonMapperUtil;
import com.netto.service.desc.ServerDesc;

public class ServiceRegistry {

    private HttpClient httpClient; 
    
    private Logger logger = Logger.getLogger(ServiceRegistry.class);

    
    private int updatePeriod = 10;//更新间隔
    
    private static ScheduledExecutorService updateExecutor =
            Executors.newScheduledThreadPool(Constants.DEFAULT_SCHEDULED_EXECUTOR_POOL_SIZE,new NamedThreadFactory("ServiceRegistry")) ;
    
    private static Map<String,AppServiceSubscriber> concurrentSubscribers = new ConcurrentHashMap();
    
    public void setUpdatePeriod(int updatePeriod) {
        this.updatePeriod = updatePeriod;
    }

    private static ServiceRegistry INSTANCE = new ServiceRegistry();

    protected ServiceRegistry(){
        httpClient = HttpClientBuilder.build(5, 10);

    }
    
    public void notifyNow(AppServiceSubscriber subscriber,ServerDesc desc){
        try{
            List<ServerAddressGroup> serverAddressGroups = ServiceRegistry.this.getServerGroups(desc.getRegistry(), desc.getServerApp());
            Map<String, RouteConfig> routeMaps = ServiceRegistry.this.getRouterMap(desc.getRegistry(), desc.getServerApp());
            subscriber.notify(serverAddressGroups, routeMaps);
        }
        catch(Throwable t){
            logger.warn("error when update app service info "+desc.getServerApp(),t);
        }
    }

    public synchronized void subscribe(AppServiceSubscriber subscriber,ServerDesc desc){
        if(concurrentSubscribers.containsKey(desc.getServerApp())){
            return ;
        }
        
        this.schedule(subscriber, desc);

        
        concurrentSubscribers.put(desc.getServerApp(), subscriber);
    }
    
    public void schedule(AppServiceSubscriber subscriber,ServerDesc desc){
        Future future = updateExecutor.schedule(new Runnable(){

            @Override
            public void run() {
                try{
                    List<ServerAddressGroup> serverAddressGroups = ServiceRegistry.this.getServerGroups(desc.getRegistry(), desc.getServerApp());
                    Map<String, RouteConfig> routeMaps = ServiceRegistry.this.getRouterMap(desc.getRegistry(), desc.getServerApp());
                    subscriber.notify(serverAddressGroups, routeMaps);
                }
                catch(Throwable t){
                    logger.warn("error when update app service info "+desc.getServerApp(),t);
                }
                ServiceRegistry.this.schedule(subscriber,desc);
            }
            
        }, updatePeriod, TimeUnit.SECONDS);
        
    
    }
    

    public Map<String, RouteConfig> getRouterMap(String registry,String serverApp) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.DEFAULT_TIMEOUT)
                .setConnectionRequestTimeout(Constants.DEFAULT_TIMEOUT).setSocketTimeout(Constants.DEFAULT_TIMEOUT)
                .build();
        
        try {
            StringBuilder sb = new StringBuilder(50);
            sb.append(registry)
                    .append(registry.endsWith("/") ? "" : "/")
                    .append(serverApp).append("/routers");
            HttpGet get = new HttpGet(sb.toString());
            get.setConfig(requestConfig);
            // 创建参数队列
            HttpResponse response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity, "UTF-8");
            ObjectMapper mapper = JsonMapperUtil.getJsonMapper();
            return mapper.readValue(body, new TypeReference<Map<String, RouteConfig>>() {});
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<ServerAddressGroup> getServerGroups(String registry,String serverApp) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.DEFAULT_TIMEOUT)
                .setConnectionRequestTimeout(Constants.DEFAULT_TIMEOUT).setSocketTimeout(Constants.DEFAULT_TIMEOUT)
                .build();
        
        List<ServerAddressGroup> serverGroups = null;
        try {
            StringBuilder sb = new StringBuilder(50);
            sb.append(registry)
                    .append(registry.endsWith("/") ? "" : "/")
                    .append(serverApp).append("/servers");
            HttpGet get = new HttpGet(sb.toString());
            get.setConfig(requestConfig);
            // 创建参数队列
            HttpResponse response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity, "UTF-8");
            ObjectMapper mapper = JsonMapperUtil.getJsonMapper();
            serverGroups =  mapper.readValue(body, mapper.getTypeFactory().constructParametricType(List.class,
                    mapper.getTypeFactory().constructType(ServerAddressGroup.class)));
            
            for(ServerAddressGroup serverGroup:serverGroups){
                serverGroup.setRegistry(registry);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            
        }
        
        return serverGroups;
    }
    
    public  static ServiceRegistry getInstance(){
        return INSTANCE;
    }
    
    
    
//    private List<ServiceProvider> getProviders() {
//        List<ServiceProvider> providers = new ArrayList<ServiceProvider>();
//        List<ServerAddressGroup> serverGroups = this.getServerGroups();
//        for (ServerAddressGroup serverGroup : serverGroups) {
//            TcpConnectPool pool = new TcpConnectPool(serverGroup, this.config);
//            ServiceProvider provider = new LocalServiceProvider(serverGroup.getServerDesc(), pool, this.needSignature());
//            providers.add(provider);
//        }
//        return providers;
//    }    
    
}
