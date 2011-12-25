package com.android.orm.exception;

public class UnsupportedPrimaryKeyTypeException extends UnsupportedFieldTypeException {
	
	private static final long serialVersionUID = 1L;
	
	public UnsupportedPrimaryKeyTypeException(String fieldTypeName) {
		super(fieldTypeName);
	}
	
	@Override
	public String getMessage() {
		return "FieldType : " + this.fieldTypeName + " does not supported by PrimaryKey constraints. PrimaryKeys should be Long.class!";
	}
}
