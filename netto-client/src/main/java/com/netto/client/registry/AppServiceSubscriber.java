package com.netto.client.registry;

import java.util.List;
import java.util.Map;

import com.netto.core.context.RouteConfig;
import com.netto.core.context.ServerAddressGroup;

public interface AppServiceSubscriber {

    public void notify(List<ServerAddressGroup> servers,Map<String,RouteConfig> routes);
    
    
}
