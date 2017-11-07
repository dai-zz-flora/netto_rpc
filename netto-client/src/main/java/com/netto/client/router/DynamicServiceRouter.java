package com.netto.client.router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.netto.client.pool.ConnectionPoolManager;
import com.netto.client.provider.ServiceProvider;
import com.netto.client.provider.TcpServiceProvider;
import com.netto.client.registry.AppServiceSubscriber;
import com.netto.core.context.RouteConfig;
import com.netto.core.context.RpcContext;
import com.netto.core.context.ServerAddressGroup;
import com.netto.core.util.Constants;
import com.netto.service.desc.ServerDesc;

public class DynamicServiceRouter implements AppServiceSubscriber, ServiceRouter {
    private volatile Map<String, RouteConfig> routers;
    private volatile Map<String, ServiceProvider> providerMap = new HashMap<String, ServiceProvider>();
    private ServerDesc serverDesc;
    private boolean needSignature;

    private GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    {
        poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(1);
        poolConfig.setMaxIdle(1);
        poolConfig.setMinIdle(1);
        // 从池中取连接的最大等待时间，单位ms.
        poolConfig.setMaxWaitMillis(1000);
        // 指明连接是否被空闲连接回收器(如果有)进行检验.如果检测失败,则连接将被从池中去除.
        poolConfig.setTestWhileIdle(true);
        // 每30秒运行一次空闲连接回收器
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        // 池中的连接空闲10分钟后被回收
        poolConfig.setMinEvictableIdleTimeMillis(600000);
        // 在每次空闲连接回收器线程(如果有)运行时检查的连接数量
        poolConfig.setNumTestsPerEvictionRun(10);
    }

    public DynamicServiceRouter(ServerDesc serverDesc, boolean needSignature) {
        this.serverDesc = serverDesc;
        this.needSignature = needSignature;

    }

    @Override
    public ServiceProvider findProvider() {
        return this.findProvider(this.serverDesc);
    }

    private ServiceProvider findProvider(ServerDesc serverDesc) {
        RouteConfig routeConfig = null;
        if (RpcContext.getRouterContext() != null) {
            if (serverDesc.getServerGroup() == null) {

                if (routers != null) {
                    routeConfig = this.routers.get(RpcContext.getRouterContext());
                }
            }
        }
        if (serverDesc.getServerGroup() == null) {
            if (routeConfig == null) {
                serverDesc.setServerGroup(Constants.DEFAULT_SERVER_GROUP);
            } else {
                serverDesc.setServerGroup(routeConfig.getTarget());
            }
        }
        ServiceProvider provider = this.providerMap.get(serverDesc.toString());
        if (provider != null) {
            provider.setRouteConfig(routeConfig);
        }
        return provider;
    }

    @Override
    public synchronized void notify(List<ServerAddressGroup> serverGroups, Map<String, RouteConfig> routers) {
        this.routers = routers;

        Map<String, ServiceProvider> tmpProviderMap = new HashMap<String, ServiceProvider>();
        for (ServerAddressGroup serverGroup : serverGroups) {
            ConnectionPoolManager.getInstance().updatePool(serverGroup, poolConfig);
            ServerDesc serverDesc = new ServerDesc();
            serverDesc.setRegistry(this.serverDesc.getRegistry());
            serverDesc.setServerApp(this.serverDesc.getServerApp());
            serverDesc.setServerGroup(serverGroup.getServerGroup());
            ServiceProvider provider = new TcpServiceProvider(serverDesc, ConnectionPoolManager.getInstance(),
                    needSignature);
            tmpProviderMap.put(serverDesc.toString(), provider);
        }

        this.providerMap = tmpProviderMap;

    }

}
