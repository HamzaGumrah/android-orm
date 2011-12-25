package com.android.orm.exception;

public class ForeignKeyViolation extends RuntimeException {
	
	private final String entityName;
	
	private final String fieldName;
	
	private static final long serialVersionUID = 1L;
	
	public ForeignKeyViolation(String entityName, String fieldName) {
		super();
		this.entityName = entityName;
		this.fieldName = fieldName;
	}
	
	@Override
	public String getMessage() {
		return "Field : " + this.fieldName + " of the " + this.entityName + " should implement Persistable and should have Entity Annotation for foreignKey support.";
	}
}
