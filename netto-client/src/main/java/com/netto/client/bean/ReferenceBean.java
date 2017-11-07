package com.netto.client.bean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netto.client.RpcHttpClient;
import com.netto.client.RpcTcpClient;
import com.netto.client.api.ServiceAPIClient;
import com.netto.client.provider.ServiceProvider;
import com.netto.client.router.ServiceRouter;
import com.netto.client.rpc.JsonMessageDecoder;
import com.netto.client.rpc.JsonMessageEncoder;
import com.netto.client.rpc.MessageDecoder;
import com.netto.client.rpc.MessageEncoder;
import com.netto.client.rpc.RpcInvoker;
import com.netto.client.transport.DefaultTransportFactory;
import com.netto.client.transport.TransportFactory;
import com.netto.core.filter.InvokeMethodFilter;

public class ReferenceBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {

    private Class<?> interfaceClazz;
    private String serviceName;
    private int timeout;
    private ServiceRouter router;
    private List<InvokeMethodFilter> filters;

    private String encoding = "json";

    private Logger logger = Logger.getLogger(ReferenceBean.class);

    private MessageEncoder messageEncoder = new JsonMessageEncoder();;

    private MessageDecoder messageDecoder = new JsonMessageDecoder();
    private ApplicationContext applicationContext;
    private TransportFactory transportFactory;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<InvokeMethodFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<InvokeMethodFilter> filters) {
        this.filters = filters;
    }

    public ServiceRouter getRouter() {
        return router;
    }

    public void setRouter(ServiceRouter router) {
        this.router = router;
    }

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

    public Class<?> getObjectType() {
        return this.interfaceClazz;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object getObject() throws Exception {
        // ServiceProvider provider = this.router.findProvider();
        // if (this.timeout <= 0) {
        // ServiceAPIClient apiClient = new ServiceAPIClient(provider, this,
        // 1000);
        // this.timeout = apiClient.getServerTimeout(this.serviceName);
        // }

        InvocationHandler client;
        client = new RpcInvoker(router, serviceName, timeout, this.messageEncoder, this.messageDecoder,
                this.transportFactory);

        Object proxy = Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class<?>[] { interfaceClazz },
                client);
        return proxy;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.encoding.equals("json")) {
            messageEncoder = new JsonMessageEncoder();

            messageDecoder = new JsonMessageDecoder();
        }

        if (this.transportFactory == null) {
            try {
                this.transportFactory = this.applicationContext.getBean(TransportFactory.class);
            } catch (Exception t) {
                logger.warn("can't find transport factory in spring context");
            }

            if (this.transportFactory == null) {
                transportFactory = new DefaultTransportFactory();
            }
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

}
