package com.netto.client.pool;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netto.client.api.ServiceAPIClient;

import com.netto.core.context.ServerAddress;
import com.netto.core.context.ServerAddressGroup;
import com.netto.core.util.Constants;
import com.netto.core.util.JsonMapperUtil;

public class DefaultSocketConnectionPool implements SocketConnectionPool {

    
    private static Logger logger = Logger.getLogger(TcpConnectPool.class);
    private final ServerAddressGroup serverGroup;
    private GenericObjectPool<SocketConnection> pool;
    
        

    public DefaultSocketConnectionPool(ServerAddressGroup serverGroup, GenericObjectPoolConfig config) {
        this.serverGroup = serverGroup;
        if (config == null) {
            config = new GenericObjectPoolConfig();
            config.setMaxTotal(1);
            config.setMaxIdle(1);
            config.setMinIdle(1);
            // 从池中取连接的最大等待时间，单位ms.
            config.setMaxWaitMillis(1000);
            // 指明连接是否被空闲连接回收器(如果有)进行检验.如果检测失败,则连接将被从池中去除.
            config.setTestWhileIdle(true);
            // 每30秒运行一次空闲连接回收器
            config.setTimeBetweenEvictionRunsMillis(30000);
            // 池中的连接空闲10分钟后被回收
            config.setMinEvictableIdleTimeMillis(600000);
            // 在每次空闲连接回收器线程(如果有)运行时检查的连接数量
            config.setNumTestsPerEvictionRun(10);

        }
        pool = new GenericObjectPool<SocketConnection>(new ClientSocketPoolFactory(), config);

    }

    public SocketConnection getResource() {

        try {
            return this.pool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void invalidate(SocketConnection resource) {
        if (resource == null)
            return;

        try {
            this.pool.invalidateObject(resource);
        } catch (Exception e) {
            logger.error("tcp Pool destroy", e);
        }
    }

    public void release(SocketConnection resource) {
        if (resource == null)
            return;
        this.pool.returnObject(resource);
    }
    
    private class PoolableSocketConnectionImpl implements SocketConnection{        
        
        private ServerAddress serverAddress;

        public PoolableSocketConnectionImpl(GenericObjectPool<SocketConnection> ownerPool,Socket socket,ServerAddress serverAddress){
            this.ownerPool = ownerPool;
            this.socket = socket;
            this.serverAddress = serverAddress;
        }
        
        private final GenericObjectPool<SocketConnection> ownerPool;
        
        private final Socket socket;

        @Override
        public void close() throws IOException {
            logger.trace("return socket at "+serverAddress+" "+serverAddress.hashCode());
            ownerPool.returnObject(this);            
        }

        @Override
        public Socket getSocket() {
            logger.trace("use socket at "+serverAddress+" "+serverAddress.hashCode());
            return this.socket;
        }

        @Override
        public boolean isClosed() {
           
            return socket == null || socket.isClosed();
        }

        @Override
        public void invalidate() {
            logger.trace("invalidate socket at "+serverAddress+" "+serverAddress.hashCode());
            try{
                ownerPool.invalidateObject(this);
            }
            catch(Exception e){
                logger.error("error when invalidate connection",e);
            }
            
        }

        
    }

    private class ClientSocketPoolFactory implements PooledObjectFactory<SocketConnection> {

        private AtomicInteger autoIndex = new AtomicInteger();

        
        public ClientSocketPoolFactory() {
            
        }

        public PooledObject<SocketConnection> makeObject() throws Exception {

            
            int tries = 0;
            while(tries++ <= serverGroup.getServers().size()+1){
                ServerAddress server = null;
                try{
                    int currentIndex = autoIndex.incrementAndGet();
                    currentIndex = serverGroup.getServers().size()%currentIndex;
                    server = serverGroup.getServers().get(currentIndex);
                    Socket socket = new Socket(server.getIp(), server.getPort());
                    
                    PooledObject<SocketConnection> po = new DefaultPooledObject<SocketConnection>(
                          new PoolableSocketConnectionImpl(pool,socket,server));  
                    
                    return po;
                }
                catch(Throwable t){
                  logger.error("tcpPool makeObject[" + server!=null?server.getIp():"" + ":" + server!=null?server.getPort():"" + "] failed! "
                  + t.getMessage(),t);
                }
            }
            
            throw new ConnectionCreateException("all the server is unavaliable");
            

        }

        public void destroyObject(PooledObject<SocketConnection> p) throws Exception {
            p.getObject().close();

        }

        public boolean validateObject(PooledObject<SocketConnection> p) {
            boolean validated = p.getObject().getSocket().isConnected() && !p.getObject().isClosed();
            return validated;
//            if (!validate) {
//                // 确认无效的直接返回就可以了
//                return validate;
//            } else {
//                // 无法确认无效需要进一步处理
//                return this.ping(p.getObject(), 1000);
//            }
        }

        public void activateObject(PooledObject<SocketConnection> p) throws Exception {
            if (p.getObject().isClosed()) {
                p.getObject().getSocket().connect(p.getObject().getSocket().getRemoteSocketAddress(), p.getObject().getSocket().getSoTimeout());
            }

        }

        public void passivateObject(PooledObject<SocketConnection> p) throws Exception {
        }

        private boolean ping(Socket socket, int timeout) {
            try {
                String data = "ping";
                int len = new ServiceAPIClient(socket, timeout).pingService(data);
                if (logger.isDebugEnabled()) {
                    logger.debug("ping server[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()
                            + "] success! return  len=" + len);
                }
                return len == data.length();
            } catch (Throwable t) {
                logger.error("ping service[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()
                        + "] failed! " + t.getMessage());
                return false;
            }
        }
 
    }

    @Override
    public SocketConnection getConnection() {
        try {
            return this.pool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private class SingleUnclosedSocketConnectionPool implements SocketConnectionPool {
        
        private Socket socket;

        private SingleUnclosedSocketConnectionPool(Socket socket){
            this.socket = socket;
        }
        
        @Override
        public SocketConnection getConnection() {
            return new SimpleUnclosedSocketConnection(socket);
        }

        @Override
        public void close() {
            // TODO Auto-generated method stub
            
        }

    }
    
    
    private class SimpleUnclosedSocketConnection implements SocketConnection{        
        
        public SimpleUnclosedSocketConnection(Socket socket){

            this.socket = socket;
        }

        
        private final Socket socket;

        @Override
        public void close() throws IOException {
            socket.close();   
        }

        @Override
        public Socket getSocket() {
            return this.socket;
        }

        @Override
        public boolean isClosed() {
           
            return socket == null || socket.isClosed();
        }

        @Override
        public void invalidate() {
            // TODO Auto-generated method stub
            
        }

        
    }

    @Override
    public void close() {
        this.pool.close();
        
    }
}
