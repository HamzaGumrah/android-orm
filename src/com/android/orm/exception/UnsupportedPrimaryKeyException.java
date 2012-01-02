package com.android.orm.exception;

public class UnsupportedPrimaryKeyException extends PrimaryKeyException {
	
	private static final long serialVersionUID = 1L;
	
	private final String fieldTypeName;
	public UnsupportedPrimaryKeyException(String entityName,String fieldTypeName) {
		super(entityName);
		this.fieldTypeName = fieldTypeName;
	}
	
	@Override
	public String getMessage() {
		return this.entityName+" has id field with type of "+this.fieldTypeName+ "'id' field is reserved by primary keys and should have Long.class! type";
	}
}
