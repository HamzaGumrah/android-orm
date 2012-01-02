package com.android.orm.exception;

public class PrimaryKeyNotFoundException extends PrimaryKeyException{
	
	private static final long serialVersionUID = 1L;

	public PrimaryKeyNotFoundException(String entityName) {
		super(entityName);
	}
	
	@Override
	public String getMessage() {
		return this.entityName + " nor its superclasses have no primary key fields. Long id field will be mapped as primary key. Please define id field.";
	}
}
