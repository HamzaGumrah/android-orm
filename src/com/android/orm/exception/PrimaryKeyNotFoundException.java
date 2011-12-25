package com.android.orm.exception;

public class PrimaryKeyNotFoundException extends RuntimeException {
	
	private final String entityName;
	
	private static final long serialVersionUID = 1L;
	
	public PrimaryKeyNotFoundException(String entityName) {
		super();
		this.entityName = entityName;
	}
	
	@Override
	public String getMessage() {
		return this.entityName + " has no primary key fields. Current implementation requires primary key for each entity object.";
	}
}
