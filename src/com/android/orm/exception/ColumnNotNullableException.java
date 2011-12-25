package com.android.orm.exception;

public class ColumnNotNullableException extends NullPointerException {
	
	private final String columnName;
	
	private static final long serialVersionUID = 1L;
	
	public ColumnNotNullableException(String columnName) {
		super();
		this.columnName = columnName;
	}
	
	@Override
	public String getMessage() {
		return this.columnName + " can not be null";
	}
}
