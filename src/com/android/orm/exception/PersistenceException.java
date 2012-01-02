package com.android.orm.exception;

import com.android.orm.Persistable;

public class PersistenceException extends RuntimeException {
	
	protected final String message;
	
	protected final Persistable persistable;
	
	private static final long serialVersionUID = 1L;
	
	public PersistenceException(Persistable persistable, String message) {
		super();
		this.message = message;
		this.persistable = persistable;
	}
	
	@Override
	public String getMessage() {
		return "Can not persist " + this.persistable.getClass().getName() + " " + this.message;
	}
}
