package com.netto.client.provider;

public abstract class AbstractServiceProvider implements ServiceProvider {
	private String registry; // 注册中心
	private String serviceApp; // 服务APP
	private String serviceGroup; // 服务APP下的服务分组

	public AbstractServiceProvider(String registry, String serviceApp, String serviceGroup) {
		this.registry = registry;
		this.serviceApp = serviceApp;
		this.serviceGroup = serviceGroup;
	}

	public String getRegistry() {
		return registry;
	}

	public String getServiceApp() {
		return serviceApp;
	}

	public String getServiceGroup() {
		return serviceGroup;
	}
}