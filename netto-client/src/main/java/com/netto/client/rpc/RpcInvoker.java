package com.netto.client.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;

import com.netto.client.provider.ServiceProvider;
import com.netto.client.router.ServiceRouter;
import com.netto.client.transport.Transport;
import com.netto.client.transport.TransportFactory;
import com.netto.core.exception.RemoteAccessException;
import com.netto.core.filter.InvokeMethodFilter;
import com.netto.core.message.NettoFrame;

/**
 * a proxy to invoke remote provider
 * 
 * @author daizz
 *
 */
public class RpcInvoker implements InvocationHandler {

 
    private ServiceRouter router;
    private int timeout;
    private String serviceName;
    
    private MessageEncoder messageEncoder ;
    
    private MessageDecoder messageDecoder;
 
    private TransportFactory transportFactory;


    public RpcInvoker(ServiceRouter router, String serviceName,
            int timeout,MessageEncoder encoder,MessageDecoder messageDecoder,TransportFactory transportFactory){
        this.router = router;
        this.timeout = timeout;
        this.serviceName = serviceName;
        this.messageEncoder = encoder;
        this.messageDecoder = messageDecoder;
        this.transportFactory = transportFactory;
    }
    
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        ServiceProvider provider = this.router.findProvider();
        
        if(provider==null)
            throw new RemoteAccessException("no provider for "+serviceName);
        
        NettoFrame requestFrame = this.messageEncoder.encode(serviceName,method, args,provider.needSignature());
        
        NettoFrame resultFrame =  this.invoke(provider, serviceName,method,requestFrame, timeout);
        
        return this.messageDecoder.decode(serviceName, method, resultFrame);
        
        
    }
    
    protected NettoFrame invoke(ServiceProvider provider,String serviceName, Method method,NettoFrame requestFrame ,int timeout){
        Transport transport = this.transportFactory.createTransport(provider, timeout);
        return transport.request(requestFrame);
    }

    

}
