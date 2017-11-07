package com.netto.client.pool;

public class ConnectionCreateException extends RuntimeException  {
    


    /**
	 * 
	 */
	private static final long serialVersionUID = 9020595371228596841L;


	/**
     * Constructor for RemoteAccessException.
     * @param msg the detail message
     */
    public ConnectionCreateException(String msg) {
        super(msg);
    }


    public ConnectionCreateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
