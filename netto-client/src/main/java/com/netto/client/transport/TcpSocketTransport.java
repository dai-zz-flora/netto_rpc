package com.netto.client.transport;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import com.netto.client.pool.SocketConnection;
import com.netto.client.pool.SocketConnectionPool;
import com.netto.core.exception.BadFrameException;
import com.netto.core.exception.RemoteAccessException;
import com.netto.core.message.NettoFrame;

public class TcpSocketTransport implements Transport{
    
    
    private SocketConnectionPool pool;
    private int timeout;
    private static Logger logger = Logger.getLogger(TcpSocketTransport.class);


    public TcpSocketTransport(SocketConnectionPool pool,int timeout){
        this.pool =   pool;
        this.timeout = timeout;
    }
  

    @Override
    public NettoFrame request(NettoFrame requestFrame) {
        SocketConnection connection = null;
        boolean invalidatedSocket = false;

        
        try {
            connection = this.pool.getConnection();
            connection.getSocket().setSoTimeout(timeout);
            OutputStream os = connection.getSocket().getOutputStream();
           
            os.write(requestFrame.getHeaderLine());
            os.write(requestFrame.getHeaderContent());
            os.flush();
            os.write(requestFrame.getBody());

            os.flush();
            byte[] headerBytes = new byte[NettoFrame.HEADER_LENGTH];
            InputStream is = connection.getSocket().getInputStream();
            Arrays.fill(headerBytes, (byte) ' ');
            is.read(headerBytes);
            
            NettoFrame responseFrame = new NettoFrame();
            responseFrame.setHeaderLine(headerBytes);
            
            String responseHeader = new String(headerBytes,"utf-8");
            
            if(!responseHeader.startsWith(NettoFrame.NETTO_HEADER_START)){
                throw new BadFrameException("error header start:"+responseHeader);
            }               
            else{
                String[] headerSections = responseHeader.substring(NettoFrame.NETTO_HEADER_START.length()).split("/");
                if (headerSections.length == 3) {
                    String flag = headerSections[0];
                    String headerContentSizeAsString = headerSections[1].trim();
                    String bodySizeAsString = headerSections[2].trim();
                    int headerContentSize = Integer.parseInt(headerContentSizeAsString);
                    int bodySize = Integer.parseInt(bodySizeAsString);
                    
                    byte[] headerContent = new byte[headerContentSize];
                    is.read(headerContent);
                    
                    responseFrame.setHeaderContent(headerContent);
                    responseFrame.setHeaderContentSize(headerContentSize);                   
                    byte[] body = new byte[bodySize];
                    int len = 0;
                    while(len<bodySize){
                        int readLen = is.read(body, len, bodySize-len);
                        if(readLen<0){
                            break;
                        }
                        else{
                            len = len + readLen;
                        }
                    }

                    responseFrame.setBody(body);
                    responseFrame.setBodySize(body.length);
                    
                    Map<String,String> headers = responseFrame.decodeHeader();
                    if(flag.equals(NettoFrame.NETTO_FAILED)){
                        String errorMessage = headers.get(NettoFrame.ERROR_HEADER);
                        throw new RemoteAccessException(errorMessage);
                    }                    
                    
                } else {
                    throw new RemoteAccessException("error header start:"+responseHeader);
                }
            }
            
            return responseFrame;
            
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            if (connection !=null &&(e instanceof SocketException || e instanceof SocketTimeoutException || e instanceof BadFrameException)) {
                try {
                    connection.close();
                } catch (IOException ioe) {
                    logger.error(e.getMessage(), e);
                }
                connection.invalidate();
                invalidatedSocket = true;
            }
            throw new RemoteAccessException("error when get response ",e);
        } finally {
            if(!invalidatedSocket&&connection!=null)
                try {
                    connection.close();
                } catch (IOException ioe) {
                    logger.error("error when close connection", ioe);
                }
        }
    }

  

}
