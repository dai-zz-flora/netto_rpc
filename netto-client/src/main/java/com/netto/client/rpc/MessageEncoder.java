package com.netto.client.rpc;

import java.lang.reflect.Method;

import com.netto.core.message.NettoFrame;

public interface MessageEncoder {

    public NettoFrame encode(String serviceName,Method method,Object[] args,boolean needSignature);
}
