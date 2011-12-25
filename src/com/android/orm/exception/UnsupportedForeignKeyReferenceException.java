package com.android.orm.exception;

public class UnsupportedForeignKeyReferenceException extends RuntimeException {
	
	private final String fieldName;
	
	private final String entityName;
	
	private final String reference;
	
	private final String referenceType;
	
	private static final long serialVersionUID = 1L;
	
	public UnsupportedForeignKeyReferenceException(String entityName, String fieldName, String reference, String referenceType) {
		super();
		this.fieldName = fieldName;
		this.entityName = entityName;
		this.reference = reference;
		this.referenceType = referenceType;
	}
	
	@Override
	public String getMessage() {
		return "ForeignKeyReferences should be primitive types,String or Enum  ForeignKey at " + this.entityName + " ->" + this.fieldName + " has reference value " + reference
				+ " which violates the rule it's Class<?> : " + referenceType;
	}
}
