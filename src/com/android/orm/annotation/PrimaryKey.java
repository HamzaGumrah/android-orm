package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * by default column name is set to _id and it is auto incremental. 
 * @author Hamza Gumrah
 *
 */
public @interface PrimaryKey {
	
}
