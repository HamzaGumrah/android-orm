package com.android.orm.adapter;

import java.lang.reflect.Method;

import com.android.orm.annotation.OneToMany;


final class OneToManyMetaData {
	
	private final Method getMethod;
	
	private final Method setMethod;
	
	private final OneToMany self;
	
	public OneToManyMetaData(OneToMany self, Method getMethod, Method setMethod) {
		this.self = self;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}
	
	public Class<?> getTargetEntity() {
		return self.targetEntityClass();
	}
	public String getMapKey(){
		return self.mapKey();
	}
	public boolean isMap(){
		if(self.mapKey().equals(""))
			return false;
		return true;
	}
	public Method getGetter() {
		return this.getMethod;
	}
	
	public Method getSetter() {
		return this.setMethod;
	}
}
