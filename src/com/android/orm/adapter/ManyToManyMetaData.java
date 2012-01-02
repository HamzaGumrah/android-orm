package com.android.orm.adapter;

import java.lang.reflect.Method;


final class ManyToManyMetaData {
	
	private final Class<?> targetEntity;
	
	private final Method getMethod;
	
	private final Method setMethod;
	
	public ManyToManyMetaData(Class<?> targetEntity, Method getMethod, Method setMethod) {
		this.targetEntity = targetEntity;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}
	
	public Class<?> getTargetEntity() {
		return targetEntity;
	}
	
	public Method getGetter() {
		return this.getMethod;
	}
	
	public Method getSetter() {
		return this.setMethod;
	}
	
}