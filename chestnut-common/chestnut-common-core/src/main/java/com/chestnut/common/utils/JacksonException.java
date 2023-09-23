package com.chestnut.common.utils;

public class JacksonException extends RuntimeException {
	
	private static final long serialVersionUID = -6243633891270839926L;

	public JacksonException() {
        super();
    }
    
    public JacksonException(String errMsg) {
        super(errMsg);
    }
    
    public JacksonException(Throwable throwable) {
        super(throwable);
    }
    
    public JacksonException(String errMsg, Throwable throwable) {
        super(errMsg, throwable);
    }
}