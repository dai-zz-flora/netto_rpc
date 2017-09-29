package com.netto.server.exception;

public class RemoteAccessException extends RuntimeException  {
    


    /**
     * Constructor for RemoteAccessException.
     * @param msg the detail message
     */
    public RemoteAccessException(String msg) {
        super(msg);
    }


    public RemoteAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
