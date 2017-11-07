package com.netto.core.context;

import com.netto.core.util.Constants;

public class RouteConfig {
	private String serverApp;
	private String routerExpression;

    private String targetRegistry = Constants.DEFAULT_TARGET_REGISTRY;
	private String targetAddress = Constants.DEFAULT_SERVER_GROUP;

	public String getServerApp() {
		return serverApp;
	}

	public void setServerApp(String serverApp) {
		this.serverApp = serverApp;
	}

	
	public String getTargetRegistry() {
		return targetRegistry;
	}

	public void setTargetRegistry(String targetRegistry) {
		this.targetRegistry = targetRegistry;
	}

	public String getTarget() {
		return targetAddress;
	}

	public void setTarget(String targetServerGroup) {
		this.targetAddress = targetServerGroup;
	}
	
    public String getRouterExpression() {
        return routerExpression;
    }

    public void setRouterExpression(String routerExpression) {
        this.routerExpression = routerExpression;
    }
	

}
