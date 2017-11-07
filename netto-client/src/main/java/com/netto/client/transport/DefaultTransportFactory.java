package com.netto.client.transport;

import org.apache.http.client.HttpClient;

import com.netto.client.http.HttpClientBuilder;
import com.netto.client.provider.ServiceProvider;
import com.netto.core.util.Constants;

public class DefaultTransportFactory implements TransportFactory {
    
    
    
    private int maxConnections = 30;
    private HttpClient httpClient;

    public DefaultTransportFactory(){
        this.initHttpClient();
    }
    
    private void initHttpClient(){    
        
        
        this.httpClient = HttpClientBuilder.build(maxConnections, maxConnections*10);

    }

    public Transport createTransport(ServiceProvider provider,int timeout){
        if(provider.getRouteConfig()==null||provider.getRouteConfig().getTargetRegistry().equals(Constants.DEFAULT_TARGET_REGISTRY)){
            return new TcpSocketTransport(provider.getConnectionPool(),timeout);
        }
        else {
            return new HttpTransport(provider,timeout,httpClient);
        }
    }
    
   
}
