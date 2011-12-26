package com.android.orm.adapter;

import java.lang.reflect.Method;

import com.android.orm.type.OrderType;

/**
 * keeps meta data for primary keys OrderType e.f DESC
 * 
 * @author Hamza Gumrah
 */
final class PrimaryKeyMetaData {
	
	private final OrderType orderType;
	
	private final Method getMethod;
	
	private final Method setMethod;
	
	PrimaryKeyMetaData(OrderType orderType, Method getMethod, Method setMethod) {
		this.orderType = orderType;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}
	
	OrderType getOrderType() {
		return this.orderType;
	}
}
