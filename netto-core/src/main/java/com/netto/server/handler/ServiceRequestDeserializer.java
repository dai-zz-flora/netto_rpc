package com.netto.server.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.netto.context.ServiceRequest;

public class ServiceRequestDeserializer implements JsonDeserializer<ServiceRequest> {

    private Logger logger = Logger.getLogger(ServiceRequestDeserializer.class);

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

    @Override
    public ServiceRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        ServiceRequest request = new ServiceRequest();
        if (json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();
            String methodName = o.get("methodName").getAsString();
            String serviceName = o.get("serviceName").getAsString();

            request.setMethodName(methodName);
            request.setServiceName(serviceName);
            String key = serviceName + "/" + methodName;

            JsonElement args = o.get("args");
            if (args == null || args.isJsonNull()) {
                logger.warn("args is null:"+key);
            } else if (args.isJsonArray()) {
                JsonArray argsArray = args.getAsJsonArray();
                String methodKey = key+"/"+argsArray.size();
                        
                if (this.serviceMethodParameterTypesCache.containsKey(methodKey)) {
                    Type[] types = this.serviceMethodParameterTypesCache.get(methodKey).types;
                    if (types.length != argsArray.size()) {
                        throw new JsonParseException("method parameters mismatch:" + methodKey);
                    } else if (types.length != 0) {
                        List<Object> argsO = new ArrayList(types.length);
                        for (int i = 0; i < types.length; i++) {
                            Object arg = context.deserialize(argsArray.get(i), types[i]);
                            argsO.add(arg);
                        }

                        request.setArgs(argsO);
                    }
                } else {
                    throw new JsonParseException("missing method parameters description:" + key);
                }
            } else {
                throw new JsonParseException("can't deserialize  ServiceRequest args:" + key);
            }
        } else {
            throw new JsonParseException("can't deserialize as ServiceRequest");
        }

        return request;
    }

}
