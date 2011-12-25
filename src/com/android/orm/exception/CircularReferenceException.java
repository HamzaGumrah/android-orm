package com.android.orm.exception;

/**
 * if there exist a circular reference in entities it is not possible to write Create Table statements we need to exit database initialization in such cases.
 * 
 * @author Hamza Gumrah
 */
public class CircularReferenceException extends RuntimeException {
	
	private final String message;
	
	public CircularReferenceException(String message) {
		super();
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
	
}
