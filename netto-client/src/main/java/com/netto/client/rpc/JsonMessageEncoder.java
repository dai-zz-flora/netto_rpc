package com.netto.client.rpc;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netto.core.exception.MessageEncodeException;
import com.netto.core.message.NettoFrame;
import com.netto.core.util.JsonMapperUtil;
import com.netto.core.util.SignatureVerifier;

public class JsonMessageEncoder implements MessageEncoder {

    private Logger logger = Logger.getLogger(JsonMessageEncoder.class);

    @Override
    public NettoFrame encode(String serviceName, Method method, Object[] args, boolean needSignature) {

        try {
            ObjectMapper mapper = JsonMapperUtil.getJsonMapper();

            String requestBody = mapper.writeValueAsString(args);
            byte[] byteBody = requestBody.getBytes("UTF-8");
            StringWriter headerWriter = new StringWriter(128);
            headerWriter.append(NettoFrame.SERVICE_HEADER).append(":").append(serviceName).append("\r\n");
            headerWriter.append(NettoFrame.METHOD_HEADER).append(":").append(method.getName()).append("\r\n");
            if (args != null) {
                headerWriter.append(NettoFrame.ARGSLEN_HEADER).append(":").append(args.length + "");
            } else {
                headerWriter.append(NettoFrame.ARGSLEN_HEADER).append(":0");
            }
            NettoFrame frame = new NettoFrame();
            
            if (needSignature) {
                headerWriter.append("\r\n");
                String signature = this.createSignature(requestBody);
                headerWriter.append(NettoFrame.SIGNATURE_HEADER).append(":").append(signature);
                frame.setSignature(signature);
            }

            byte[] headerContentBytes = headerWriter.toString().getBytes("UTF-8");

            String header = String.format("%s%d/%d/%d", NettoFrame.NETTO_HEADER_START, 2, headerContentBytes.length,
                    byteBody.length);
            byte[] headerBytes = new byte[NettoFrame.HEADER_LENGTH];

            Arrays.fill(headerBytes, (byte) ' ');
            System.arraycopy(header.getBytes("utf-8"), 0, headerBytes, 0, header.length());


            frame.setHeaderLine(headerBytes);
            frame.setHeaderContent(headerContentBytes);
            frame.setHeaderContentSize(headerContentBytes.length);
            frame.setBody(byteBody);
            frame.setBodySize(byteBody.length);
            frame.setServiceName(serviceName);
            frame.setMethodName(method.getName());

            if(args != null)
                frame.setArgsLen(args.length);
            
            return frame;

        } catch (Throwable t) {
            logger.error(" message encode error ", t);
            throw new MessageEncodeException(" message encode error ", t);
        }

    }

    protected String createSignature(String data) throws UnsupportedEncodingException {
        return SignatureVerifier.createSignature(data);
    }

}
