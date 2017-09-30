package com.netto.demo.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.netto.context.ServiceRequest;
import com.netto.demo.Book;
import com.netto.demo.HelloService;
import com.netto.demo.User;
import com.netto.demo.impl.HelloServiceImpl;
import com.netto.server.NettyServer;
import com.netto.server.handler.ServiceRequestJacksonDeserializer;

public class NettoServer {

   
    
    
	public static void main(String[] args) throws Exception {
        Map<String, Object> refBeans = new HashMap<String, Object>();
        refBeans.put("helloService", new HelloServiceImpl());
        NettyServer server = new NettyServer(9229);
        server.setMaxWaitingQueueSize(2);
        server.setNumOfHandlerWorker(1);
        server.setMaxRequestSize(1024*1024);
        server.setNumOfIOWorkerThreads(1);
        server.setRefBeans(refBeans);
        server.afterPropertiesSet();

	}

}
