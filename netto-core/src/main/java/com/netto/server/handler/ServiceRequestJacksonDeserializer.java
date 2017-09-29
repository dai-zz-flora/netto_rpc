package com.netto.server.handler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.netto.context.ServiceRequest;

public class ServiceRequestJacksonDeserializer extends StdDeserializer<ServiceRequest> {
    

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final ObjectMapper mapper ; 

    public ServiceRequestJacksonDeserializer(Class<?> vc,ObjectMapper mapper) {
        super(vc);
        this.mapper = mapper;
    }

    private Logger logger = Logger.getLogger(ServiceRequestJacksonDeserializer.class);

    private Map<String, ServiceMethodDesc> serviceMethodParameterTypesCache = new ConcurrentHashMap();

    // private static final ThreadLocal<ServiceMethodDesc> _localContext = new
    // ThreadLocal<>();

    private class ServiceMethodDesc {
        String serviceMethod;
        Type[] types;

        public ServiceMethodDesc(String serviceMethod, Type[] types) {
            super();
            this.serviceMethod = serviceMethod;
            this.types = types;
        }

    }

    public boolean registerMethodParameterTypes(String service, Class<?> clazz) {

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {

            Type[] parameterTypes = method.getGenericParameterTypes();
            String key = service + "/" + method.getName() + "/" + parameterTypes.length;
            this.serviceMethodParameterTypesCache.put(key, new ServiceMethodDesc(key, parameterTypes));
            
            
            String defaultKey = service + "/" + method.getName() + "/";
            this.serviceMethodParameterTypesCache.put(defaultKey, new ServiceMethodDesc(key, parameterTypes));
        }

        return false;

    }

    public boolean registerMethodParameterTypes(String service, String methodName, Type[] parameterTypes) {
        String key = service + "/" + methodName;

        if (this.serviceMethodParameterTypesCache.containsKey(key)) {
            return false;
        } else {
            this.serviceMethodParameterTypesCache.put(key, new ServiceMethodDesc(key, parameterTypes));
        }

        return true;
    }

    // public void setCurrentContext(String serviceMethod){
    // if(this.serviceMethodParameterTypesCache.containsKey(serviceMethod)){
    // _localContext.set(this.serviceMethodParameterTypesCache.get(serviceMethod));
    // }
    // else{
    // throw new JsonParseException("missing service method
    // description:"+serviceMethod);
    // }
    // }
    //
    //
    // public void clearCurrentContext(String serviceMethod){
    // _localContext.remove();
    // }

 
//    public ServiceRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
//            throws JsonParseException {
//        ServiceRequest request = new ServiceRequest();
//        if (json.isJsonObject()) {
//            JsonObject o = json.getAsJsonObject();
//            String methodName = o.get("methodName").getAsString();
//            String serviceName = o.get("serviceName").getAsString();
//
//            request.setMethodName(methodName);
//            request.setServiceName(serviceName);
//            String key = serviceName + "/" + methodName;
//
//            JsonElement args = o.get("args");
//            if (args == null || args.isJsonNull()) {
//                logger.warn("args is null:"+key);
//            } else if (args.isJsonArray()) {
//                JsonArray argsArray = args.getAsJsonArray();
//                String methodKey = key+"/"+argsArray.size();
//                        
//                if (this.serviceMethodParameterTypesCache.containsKey(methodKey)) {
//                    Type[] types = this.serviceMethodParameterTypesCache.get(methodKey).types;
//                    if (types.length != argsArray.size()) {
//                        throw new JsonParseException("method parameters mismatch:" + methodKey);
//                    } else if (types.length != 0) {
//                        List<Object> argsO = new ArrayList(types.length);
//                        for (int i = 0; i < types.length; i++) {
//                            Object arg = context.deserialize(argsArray.get(i), types[i]);
//                            argsO.add(arg);
//                        }
//
//                        request.setArgs(argsO);
//                    }
//                } else {
//                    throw new JsonParseException("missing method parameters description:" + key);
//                }
//            } else {
//                throw new JsonParseException("can't deserialize  ServiceRequest args:" + key);
//            }
//        } else {
//            throw new JsonParseException("can't deserialize as ServiceRequest");
//        }
//
//        return request;
//    }

    @Override
    public ServiceRequest deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        long id = 0;
        String methodName = null;
        String serviceName = null;
        JsonToken currentToken = null;
        int argsLen = -1;
        List<Object> args = null;
        try{
        while ((currentToken = jp.nextValue()) != null) {
//            System.out.println(currentToken);
//            System.out.println(jp.getCurrentName());
            switch (currentToken) {
                case VALUE_STRING:
                    switch (jp.getCurrentName()) {
                        case "methodName":
                            methodName = jp.getText();
                            break;
                        case "serviceName":
                            serviceName = jp.getText();
                            break;
                        default:
                            break;
                    }
                    break; 
                case VALUE_NUMBER_INT:
                    if (jp.getCurrentName().equals("argsLength")) {
                        argsLen = jp.getValueAsInt();
                        break;    
                    }
                                         
                case START_ARRAY:
                    args = this.readArgs(jp, ctxt, serviceName, methodName, argsLen);
                default:
                    break;
            }
        }
        }
        catch(Throwable t){
            logger.error("error when parse",t);
            throw t;
        }
        ServiceRequest request = new ServiceRequest();
        request.setMethodName(methodName);
        request.setServiceName(serviceName);
        request.setArgs(args);
        return request;
    }
    
    
    private List<Object> readArgs(JsonParser jp, DeserializationContext ctxt,String serviceName,String methodName,int argsLen) throws IOException{
        String methodKey =  serviceName + "/" + methodName + "/" ;
        if(argsLen!=-1){
            methodKey = methodKey+argsLen;
        }
        Type[] types = null;
        if(this.serviceMethodParameterTypesCache.containsKey(methodKey)){
            ServiceMethodDesc desc = this.serviceMethodParameterTypesCache.get(methodKey);
            types = desc.types;
        }
        else{
            throw new JsonParseException(jp,"so such method:"+methodKey);
        }
        
        int length = types!=null?types.length:0;
        int currentIndex = 0;
        List<Object> args = new ArrayList(length);
        
        JsonToken currentToken = null;
        while ((currentToken = jp.nextValue()) != null) {
            switch(currentToken){
                case END_ARRAY:
                    if(jp.getCurrentName().equals("args"))
                        return args;
                default:
                    if(currentIndex<length){
                        args.add(ctxt.readValue(jp, mapper.getTypeFactory().constructType(types[0])));
                        currentIndex++;
                    }
            }
            
        }
        
        return args;
    }

}
