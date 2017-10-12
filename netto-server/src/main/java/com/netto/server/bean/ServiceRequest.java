package com.netto.server.bean;

public class ServiceRequest {
	private String serviceName;
	private String methodName;
	private Object[] args;
	private long requestSeqence = 0;

	public long getRequestSeqence() {
        return requestSeqence;
    }

    public void setRequestSeqence(long requestSeqence) {
        this.requestSeqence = requestSeqence;
    }

    public void setArgs(Object[] args) {
		this.args = args;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArgs() {
		return args;
	}

}
