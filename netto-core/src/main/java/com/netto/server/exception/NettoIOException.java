package com.netto.server.exception;

public class NettoIOException extends RuntimeException  {
    


    /**
     * Constructor for RemoteAccessException.
     * @param msg the detail message
     */
    public NettoIOException(String msg) {
        super(msg);
    }


    public NettoIOException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
