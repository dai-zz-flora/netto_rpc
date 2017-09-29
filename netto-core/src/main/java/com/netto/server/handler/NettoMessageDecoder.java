package com.netto.server.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class NettoMessageDecoder  extends MessageToMessageDecoder<NettoFrame>{
    
    private Logger logger = Logger.getLogger(NettoMessageDecoder.class);
    

    @Override
    protected void decode(ChannelHandlerContext ctx, NettoFrame msg, List<Object> out) throws Exception {
        ByteBuf headerContent = msg.getHeaderContent();
        byte[] headerBytesBuf = new byte[msg.getHeaderContentSize()];
        headerContent.getBytes(headerContent.readerIndex(), headerBytesBuf);
        NettoMessage message = new NettoMessage();
        ByteBuf body = msg.getBody();
        byte[] bodyBytesBuf = new byte[msg.getBodySize()];        
        body.getBytes(body.readerIndex(), bodyBytesBuf);
        
        Map<String,String> headers = this.decoderHeader(new String(headerBytesBuf));

        message.setBody(bodyBytesBuf);
        message.setHeaders(headers);
        out.add(message);
        
        
    }
    
    
    
    private Map<String,String> decoderHeader(String headerContent){
        String[] headers = StringUtils.split(headerContent, "\r\n");
        Map<String,String> headersMap = Arrays.asList(headers).stream().map(str->{
            String[] pair = str.split(":");
            if(pair.length<2){
                pair = new String[]{str,""};
                return pair;
            }            
            else{
                return pair;
            }
        }).collect(
                Collectors.toMap(pair -> pair[0], pair -> pair[1]));
        
        return headersMap;
    }

}
