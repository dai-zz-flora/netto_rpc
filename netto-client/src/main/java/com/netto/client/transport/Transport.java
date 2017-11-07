package com.netto.client.transport;

import com.netto.core.message.NettoFrame;

public interface Transport {

    public NettoFrame request(NettoFrame requestFrame);
}
