package com.android.orm.exception;

public class EntityViolation extends RuntimeException {
	
	private final String className;
	
	private final String message;
	
	private static final long serialVersionUID = 1L;
	
	public EntityViolation(String className, String message) {
		super();
		this.className = className;
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.className + " " + this.message;
	}
}
