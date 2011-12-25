package com.android.orm.exception;

public class UnsupportedFieldTypeException extends RuntimeException {
	
	protected final String fieldTypeName;
	
	private static final long serialVersionUID = 1L;
	
	public UnsupportedFieldTypeException(String fieldTypeName) {
		super();
		this.fieldTypeName = fieldTypeName;
	}
	
	@Override
	public String getMessage() {
		return "FieldType : " + this.fieldTypeName + " does not supported. FieldTypes should be primitave types , String , Enum or must implement Persistable!";
	}
}
