package com.netto.client.router;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListSelectionEvent;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.netto.client.registry.ServiceRegistry;
import com.netto.core.context.RouteConfig;
import com.netto.core.context.ServerAddressGroup;
import com.netto.core.filter.InvokeMethodFilter;

public class ServiceRouterFactory implements FactoryBean<ServiceRouter>, InitializingBean {
	private ServerAddressGroup serverGroup;
//	private Map<String, RouteConfig> routers;
	private GenericObjectPoolConfig poolConfig;
//	private List<InvokeMethodFilter> filters;
	
	private ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();


    private boolean needSignature = false;

	public boolean doNeedSignature() {
		return needSignature;
	}

	public void setNeedSignature(boolean needSignature) {
		this.needSignature = needSignature;
	}

	public GenericObjectPoolConfig getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

//	public List<InvokeMethodFilter> getFilters() {
//		return filters;
//	}
//
//	public void setFilters(List<InvokeMethodFilter> filters) {
//		this.filters = filters;
//	}
//
//	public Map<String, RouteConfig> getRouters() {
//		return routers;
//	}
//
//	public void setRouters(Map<String, RouteConfig> routers) {
//		this.routers = routers;
//	}

	public ServiceRouter getObject() throws Exception {

//		List<ServiceProvider> providers = new ArrayList<ServiceProvider>();
//
//		ServiceProvider provider = null;
//		if (serverGroup.getRegistry() != null && serverGroup.getRegistry().startsWith("http")) {
//
//			provider = new NginxServiceProvider(this.serverGroup.getServerDesc(), needSignature)
//					.setPoolConfig(this.poolConfig);
//
//		} else {
//			TcpConnectPool pool = new TcpConnectPool(serverGroup, this.poolConfig);
//			provider = new LocalServiceProvider(this.serverGroup.getServerDesc(), pool, needSignature);
//
//		}
//		providers.add(provider);

//		return new ServiceRouter(this.serverGroup.getServerDesc(), providers, this.getRouters());

	    DynamicServiceRouter router = new DynamicServiceRouter(this.serverGroup.getServerDesc(),this.needSignature);
	    if(this.serverGroup.getServers()!=null&&this.serverGroup.getServers().size()>0){
	        router.notify(Arrays.asList(this.serverGroup), new HashMap());
	    }
	    
	    if(this.serverGroup.getRegistry()!=null){
	        serviceRegistry.notifyNow(router, this.serverGroup.getServerDesc());
	        serviceRegistry.subscribe(router, this.serverGroup.getServerDesc());
	    }
	    return router;
	}

	public Class<?> getObjectType() {
		return ServiceRouter.class;
	}

	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.serverGroup == null) {
			throw new Exception("exception:serverGroup is null!");
		}

	}

	public void setServerGroup(ServerAddressGroup serverGroup) {
		this.serverGroup = serverGroup;
	}
	
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
	

}
