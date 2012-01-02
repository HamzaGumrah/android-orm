package com.android.orm.exception;


public class ManyToManyViolation extends RuntimeException {
	
	private final String fieldName;
	
	private final Class<?> entityClass;
	
	
	public ManyToManyViolation(String fieldName, Class<?> entityClass) {
		this.fieldName = fieldName;
		this.entityClass = entityClass;
	}


	@Override
	public String getMessage() {
		return "TargetEntityClass is not defined or is not an entity class , for "+entityClass.getSimpleName()+" at field : "+this.fieldName;
	}
	
}
