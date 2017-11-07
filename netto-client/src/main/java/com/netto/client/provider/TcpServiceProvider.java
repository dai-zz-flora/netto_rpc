package com.netto.client.provider;

import com.netto.client.pool.ConnectPool;
import com.netto.client.pool.ConnectionPoolManager;
import com.netto.client.pool.SocketConnectionPool;
import com.netto.service.desc.ServerDesc;

public class TcpServiceProvider extends AbstractServiceProvider {
    
    private ConnectionPoolManager connectionPoolManager;

    public TcpServiceProvider(ServerDesc serverDesc, ConnectionPoolManager connectionPoolManager,boolean needSignature) {
        super(serverDesc, needSignature);
        this.connectionPoolManager = connectionPoolManager;
    }

    @Override
    public ConnectPool<?> getPool(String protocol) {
        return null;
    }

    @Override
    public SocketConnectionPool getConnectionPool() {
       
        return connectionPoolManager.getPool(this.getServerDesc());
    }

}
