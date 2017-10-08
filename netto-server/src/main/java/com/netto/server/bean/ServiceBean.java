package com.netto.server.bean;

/**
 * 主要定义服务的规则，如限流，过滤器等
 * 
 * @author lidong
 *
 */
public class ServiceBean {
	private String refName;

	private int timeout;

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	private String serviceName;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String name) {
		this.serviceName = name;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
