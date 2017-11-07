package com.netto.core.exception;

public class MessageDecodeException extends RuntimeException  {
    


    /**
	 * 
	 */
	private static final long serialVersionUID = 9020595371228596841L;


	/**
     * Constructor for RemoteAccessException.
     * @param msg the detail message
     */
    public MessageDecodeException(String msg) {
        super(msg);
    }


    public MessageDecodeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
