package com.netto.context;

public class ServiceResponse<T> {
	private int invokeId;
	private Boolean success = false;

	
	private T retObject;

	public T getRetObject() {
        return retObject;
    }

    public void setRetObject(T retObject) {
        this.retObject = retObject;
    }

    public int getInvokeId() {
		return invokeId;
	}

	public void setInvokeId(int invokeId) {
		this.invokeId = invokeId;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}




}
