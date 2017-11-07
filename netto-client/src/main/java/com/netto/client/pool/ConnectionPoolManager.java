package com.netto.client.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.netto.core.context.ServerAddressGroup;
import com.netto.service.desc.ServerDesc;

public class ConnectionPoolManager {

    private Map<String, PoolEntry> pools = new ConcurrentHashMap();

    private static final ConnectionPoolManager INSTANCE = new ConnectionPoolManager();

    public SocketConnectionPool getPool(ServerDesc serverDesc) {
        String key = serverDesc.getServerGroup() + "-" + serverDesc.getServerApp();
        return this.pools.get(key).pool;

    }

    protected ConnectionPoolManager() {

    }

    public static synchronized ConnectionPoolManager getInstance() {
        return INSTANCE;
    }

    public synchronized void updatePool(ServerAddressGroup group, GenericObjectPoolConfig poolConfig) {
        String key = group.getServerGroup() + "-" + group.getServerApp();
        PoolEntry oldEntry = null;
        if (pools.containsKey(key)) {
            oldEntry = this.pools.get(key);
            if (oldEntry.group.equals(group)) {
                return;
            }
        }

        SocketConnectionPool pool = new DefaultSocketConnectionPool(group, poolConfig);
        PoolEntry entry = new PoolEntry();
        entry.pool = pool;
        entry.group = group;
        this.pools.put(key, entry);
        
        if(oldEntry!=null){
            oldEntry.pool.close();
        }

    }

    private class PoolEntry {
        SocketConnectionPool pool;
        ServerAddressGroup group;

    }
}
