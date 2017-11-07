package com.netto.client.rpc;

import java.lang.reflect.Method;

import com.netto.core.message.NettoFrame;

public interface MessageDecoder {

    public Object decode(String serviceName,Method method,NettoFrame frame);
}
