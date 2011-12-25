package com.android.orm.exception;

public class MultiplePrimaryKeyException extends RuntimeException {
	
	private final String entityName;
	
	private static final long serialVersionUID = 1L;
	
	public MultiplePrimaryKeyException(String entityName) {
		super();
		this.entityName = entityName;
	}
	
	@Override
	public String getMessage() {
		return this.entityName + " has more than 1 primary key fields. Current implementation only supports 1 primary key.";
	}
}
