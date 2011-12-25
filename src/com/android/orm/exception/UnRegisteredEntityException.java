package com.android.orm.exception;

public class UnRegisteredEntityException extends RuntimeException {
	
	private final String entityName;
	
	private static final long serialVersionUID = 1L;
	
	public UnRegisteredEntityException(String entityName) {
		super();
		this.entityName = entityName;
	}
	
	@Override
	public String getMessage() {
		return this.entityName + " is not registered to OrmRegistry";
	}
}
