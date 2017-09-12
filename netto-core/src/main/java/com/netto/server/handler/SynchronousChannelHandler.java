package com.netto.server.handler;

import java.util.List;
import java.util.Map;

import com.netto.filter.InvokeMethodFilter;
import com.netto.server.bean.NettoServiceBean;

import io.netty.channel.ChannelHandlerContext;

public class SynchronousChannelHandler extends AbstractServiceChannelHandler implements NettoServiceChannelHandler{


    
    public SynchronousChannelHandler(Map<String, NettoServiceBean> serviceBeans,  List<InvokeMethodFilter> filters) {
        super(serviceBeans,filters);
    }    
    
    @Override
    public void received(ChannelHandlerContext ctx, String message) throws Exception {
        this.handle(ctx, message);        
    }

}
