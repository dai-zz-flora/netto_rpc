package com.netto.client.rpc;


import java.io.File;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netto.core.exception.MessageDecodeException;
import com.netto.core.message.NettoFrame;
import com.netto.core.util.JsonMapperUtil;


public  class JsonMessageDecoder implements MessageDecoder{

    private Logger logger = Logger.getLogger(JsonMessageDecoder.class);

    public Object decodeBody(String serviceName, Method method, byte[] bodyBytes) {
        ObjectMapper mapper = JsonMapperUtil.getJsonMapper();
        try {
            return mapper.readValue(bodyBytes, mapper.getTypeFactory().constructType(method.getGenericReturnType()));
        } catch (Throwable t) {
            logger.error(" error when decode message ",t);
            throw new MessageDecodeException(t.getMessage(),t);
        }
    }

    @Override
    public Object decode(String serviceName, Method method, NettoFrame frame) {
        return this.decodeBody(serviceName, method, frame.getBody());
    }


}
