package com.android.orm.exception;

public class NullColumnException extends NullPointerException {
	
	private final String columnName;
	
	private static final long serialVersionUID = 1L;
	
	public NullColumnException(String columnName) {
		super();
		this.columnName = columnName;
	}
	
	@Override
	public String getMessage() {
		return this.columnName + " can not be null";
	}
}
