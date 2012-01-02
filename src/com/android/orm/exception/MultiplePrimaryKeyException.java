package com.android.orm.exception;

public class MultiplePrimaryKeyException extends PrimaryKeyException {
	
	private static final long serialVersionUID = 1L;

	public MultiplePrimaryKeyException(String entityName) {
		super(entityName);
	}
	
	@Override
	public String getMessage() {
		return this.entityName + " has more than 1 primary key field. Please do not create another id field if super class already has it.";
	}
}
