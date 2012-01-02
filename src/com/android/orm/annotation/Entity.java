package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * All entities should implement Persistable interface They all need to have a constructor without arguments.
 * <p><B> @Entity </B></p>
 * <p>class B implements Persistable{</p>
 *  <p>@PrimaryKey </p>
 *  <p>private Long id;</p>
 * <p>}</p>
 * @author Hamza
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
	
}
