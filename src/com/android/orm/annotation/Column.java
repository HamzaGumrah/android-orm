package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Each Column field should have a getter and setter method. 
 * 
 */
public @interface Column {
	
	String name() default "";
	
	boolean nullable() default true;
	
	/**
	 * @return length value for String fields
	 */
	int length() default 0;
}
