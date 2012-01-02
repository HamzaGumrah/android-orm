package com.android.orm.exception;


public abstract class PrimaryKeyException extends RuntimeException {
	
	protected final String entityName;
	
	protected static final long serialVersionUID = 1L;
	
	public PrimaryKeyException(String entityName) {
		super();
		this.entityName = entityName;
	}
}
