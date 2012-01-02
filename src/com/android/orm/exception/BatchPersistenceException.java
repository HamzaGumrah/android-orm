package com.android.orm.exception;


public class BatchPersistenceException extends PersistenceException {

	public BatchPersistenceException(String message) {
		super(null, message);
	}
	@Override
	public String getMessage() {
		return "Error at batchPersist : " + this.message;
	}
}
