package com.android.orm.exception;

public class PersistableNotFoundException extends IllegalArgumentException {
	
	private final String className;
	
	private static final long serialVersionUID = 1L;
	
	public PersistableNotFoundException(String className) {
		super();
		this.className = className;
	}
	
	@Override
	public String getMessage() {
		return this.className + " should implement com.droid.orm.Persistable";
	}
}
