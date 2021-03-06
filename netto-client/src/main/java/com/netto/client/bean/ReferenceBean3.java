package com.netto.client.bean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

import com.netto.client.RpcHttpClient;
import com.netto.client.RpcTcpClient;
import com.netto.client.api.ServiceAPIClient;
import com.netto.client.provider.ServiceProvider;
import com.netto.client.router.ServiceRouter;
import com.netto.core.filter.InvokeMethodFilter;

public class ReferenceBean3 implements FactoryBean<Object> {

	private Class<?> interfaceClazz;
	private String serviceName;
	private int timeout;
	private ServiceRouter router;
	private List<InvokeMethodFilter> filters;
	

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<InvokeMethodFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<InvokeMethodFilter> filters) {
		this.filters = filters;
	}

	public ServiceRouter getRouter() {
		return router;
	}

	public void setRouter(ServiceRouter router) {
		this.router = router;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Class<?> getInterfaceClazz() {
		return interfaceClazz;
	}

	public void setInterfaceClazz(Class<?> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}

//	public String getProtocol() {
//		return protocol;
//	}
//
//	public void setProtocol(String protocol) {
//		this.protocol = protocol;
//	}

	public Class<?> getObjectType() {
		return this.interfaceClazz;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
//		ServiceProvider provider = this.router.findProvider();
//		if (this.timeout <= 0) {
//			ServiceAPIClient apiClient = new ServiceAPIClient(provider, this, 1000);
//			this.timeout = apiClient.getServerTimeout(this.serviceName);
//		}
//		if (protocol.equals("tcp")) {
//			// 需要看看router的注册中心和注入的注册中心是否一致，如果不一致就把协议改为http
//			if (provider.getRouteConfig() != null) {
//				if (!provider.getServerDesc().getRegistry().equals(provider.getRouteConfig().getTargetRegistry())) {
//					protocol = "http";
//				}
//			}
//		}
//		InvocationHandler client;
//		if (protocol.equals("tcp")) {
//			client = new RpcTcpClient(provider, filters, serviceName, this.timeout);
//		} else {
//			client = new RpcHttpClient(provider, filters, this.serviceName, this.timeout);
//		}
//		Object proxy = Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class<?>[] { interfaceClazz },
//				client);
//		return proxy;
	    return null;
	}

}
