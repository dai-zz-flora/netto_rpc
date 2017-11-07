package com.netto.core.exception;

public class MessageEncodeException extends RuntimeException  {
    
    

    /**
	 * 
	 */
	private static final long serialVersionUID = 9020595371228596841L;


	/**
     * Constructor for RemoteAccessException.
     * @param msg the detail message
     */
    public MessageEncodeException(String msg) {
        super(msg);
    }


    public MessageEncodeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
