package com.netto.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class JsonMapperUtil {

    private static ObjectMapper objectMapper ;
    
    public static ObjectMapper getJsonMapper(){
        if(objectMapper==null){
            synchronized(JsonMapperUtil.class){
                if(objectMapper==null){
                    objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,false);
                }
            }
        }
        
        return objectMapper;
        
    }
    
  
}
