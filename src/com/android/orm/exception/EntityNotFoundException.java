package com.android.orm.exception;

public class EntityNotFoundException extends IllegalArgumentException {
	
	private final String className;
	
	private static final long serialVersionUID = 1L;
	
	public EntityNotFoundException(String className) {
		super();
		this.className = className;
	}
	
	@Override
	public String getMessage() {
		return this.className + " does not have com.droid.orm.annotation.Entity annotation";
	}
}
