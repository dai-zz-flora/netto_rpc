package com.netto.client.transport;

import com.netto.client.provider.ServiceProvider;

public interface TransportFactory {
          
    public Transport createTransport(ServiceProvider provider,int timeout);
    
   
}
