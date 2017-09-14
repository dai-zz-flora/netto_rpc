package com.netto.client.bean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

import com.netto.client.RpcHttpClient;
import com.netto.client.RpcTcpClient;
import com.netto.client.router.ServiceRouter;

import com.netto.filter.InvokeMethodFilter;

public class ReferenceBean implements FactoryBean<Object> {
	private String protocol = "tcp"; // tcp,http
	private Class<?> interfaceClazz;

	private String serviceName;
	
	public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    private int timeout;
//	private ServiceRouterFactory routerFactory;
	private ServiceRouter router;
    private List<InvokeMethodFilter> filters;
	
    public List<InvokeMethodFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<InvokeMethodFilter> filters) {
        this.filters = filters;
    }	

	public void setRouter(ServiceRouter router) {
        this.router = router;
    }

//    public ServiceRouterFactory getRouterFactory() {
//		return routerFactory;
//	}
//
//	public void setRouterFactory(ServiceRouterFactory routerFactory) {
//		this.routerFactory = routerFactory;
//	}

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

//	public String getServiceUri() {
//		return this.serviceUri;
//	}

//	public void setServiceUri(String serviceUri) {
//		String[] temps = serviceUri.split("/");
//		this.serviceName = temps[1];
//		this.serviceApp = temps[0];
//		this.serviceUri = serviceUri;
//	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Object getObject() throws Exception {
		InvocationHandler client;
		if (protocol.equals("tcp")) {
			client = new RpcTcpClient(this.router.findProvider(),
					filters, serviceName, this.timeout);
		} else {
			client = new RpcHttpClient(this.router.findProvider(),
					filters, this.serviceName, this.timeout);
		}
		Object proxy = Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class<?>[] { interfaceClazz },
				client);
		return proxy;
	}

	public Class<?> getObjectType() {
		return this.interfaceClazz;
	}

	public boolean isSingleton() {
		return true;
	}
}
