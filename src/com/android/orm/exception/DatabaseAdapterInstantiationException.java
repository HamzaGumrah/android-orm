package com.android.orm.exception;

public class DatabaseAdapterInstantiationException extends InstantiationException {
	
	private final String message;
	
	public DatabaseAdapterInstantiationException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return "Can not instantiate DatabaseAdapter " + this.message;
	}
}
