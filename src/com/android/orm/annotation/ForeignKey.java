package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.android.orm.OrmConstants;
import com.android.orm.type.FetchType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
	
	/**
	 * @return reference fieldName. references should be primitive types, String or Enum , primary keys strongly suggested
	 */
	String reference() default OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE;
	
	FetchType fetch() default FetchType.EAGER;
}
