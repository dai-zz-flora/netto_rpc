package com.netto.client.pool;

public interface SocketConnectionPool {

    public SocketConnection getConnection();
    
    public void close();
    
   
}
