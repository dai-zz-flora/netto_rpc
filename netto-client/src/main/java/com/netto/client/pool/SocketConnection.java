package com.netto.client.pool;

import java.io.Closeable;
import java.net.Socket;

public interface SocketConnection extends Closeable{

    public Socket getSocket();
    
    
    public boolean isClosed();
    
    
    public void invalidate();
    
    
}
