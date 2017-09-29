package com.netto.demo.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

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

    @Test
    public void testJackson() throws JsonParseException, JsonMappingException, IOException{
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        
        om.setSerializationInclusion(Include.NON_NULL);
        om.setSerializationInclusion(Include.NON_DEFAULT);
        
        SimpleModule simpleModule  = new SimpleModule();
        ServiceRequestJacksonDeserializer deserializer = new ServiceRequestJacksonDeserializer(ServiceRequest.class,om);
        deserializer.registerMethodParameterTypes("helloService",HelloService.class);
        simpleModule.addDeserializer(ServiceRequest.class,deserializer);
        
//        simpleModule.setDeserializerModifier(new BeanDeserializerModifier() {
//            @Override
//            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
//                return new DisallowNullDeserializer(beanDesc.getBeanClass(), deserializer);
//            }
//        });
        
        om.registerModule(simpleModule);
        
        User user = new User();
        
        String k = om.writeValueAsString(user);
        
        ServiceRequest r1 = new ServiceRequest();
        r1.setMethodName("updateUsers");
        r1.setServiceName("helloService");

        
        User u = new User();
        u.setName("test1");
        u.setAge(10);
        List<Book> books = new ArrayList<Book>();
        Book book = new Book();
        book.setName("人类简史");
        books.add(book);
        u.setBooks(books);
        List<User> usrs = Arrays.asList(u);
        
        r1.setArgs(Arrays.asList(usrs));
        
            
        String json = om.writeValueAsString(r1);
        
        ServiceRequest request = om.readValue(json, ServiceRequest.class);
        
        System.out.println(request);
    }
    
    
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
