package com.netto.server.message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.netto.core.message.NettoFrame;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class NettoMessageDecoder  extends MessageToMessageDecoder<NettoFrame>{
    
//    private static Logger logger = Logger.getLogger(NettoMessageDecoder.class);
    

    @Override
    protected void decode(ChannelHandlerContext ctx, NettoFrame frame, List<Object> out) throws Exception {
        try{

            byte[] bodyBytesBuf = frame.getBody();

            NettoMessage message = new NettoMessage();
            
            Map<String,String> headers = frame.decodeHeader();    
            message.setBody(bodyBytesBuf);
            message.setHeaders(headers);
            out.add(message);
        }
        finally{

        }
        
        
    }
           

}
