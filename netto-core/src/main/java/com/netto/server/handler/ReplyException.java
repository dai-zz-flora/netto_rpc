package com.netto.server.handler;

public class ReplyException extends RuntimeException  {
    


    /**
     * Constructor for RemoteAccessException.
     * @param msg the detail message
     */
    public ReplyException(String msg) {
        super(msg);
    }


    public ReplyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
