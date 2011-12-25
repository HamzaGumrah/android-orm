package com.android.orm.exception;

public class DublicatedEntityNameException extends RuntimeException {
	
	private final String entityName;
	
	private static final long serialVersionUID = 1L;
	
	public DublicatedEntityNameException(String entityName) {
		super();
		this.entityName = entityName;
	}
	
	@Override
	public String getMessage() {
		return this.entityName + " dublicated, make sure setting unique names for each Entity";
	}
}
