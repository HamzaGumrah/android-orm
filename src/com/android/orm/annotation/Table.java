package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * in a hierarchy if only one Entity has Table annotation,
 * all other child entities up to it will be merged under single table.
 * If another entity below it has another Table annotation than a joined table
 * will be generated. joined table's _id column will have a foreing key to main table's
 * primarykey("_id" column)
 * @author Hamza Gumrah
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * @return table name
	 */
	String name() default "";
}
