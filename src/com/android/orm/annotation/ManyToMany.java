package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.android.orm.OrmConstants;

/**
 * if field is a collection,set or map , else this annotation is underestimated
 * creates cross table with name : <entity>_<targetentity>_<cross>
 * this annotation is not allowed to be used together with ForeignKey and Column
 *<p><B>@Entity </B></p>
 *<p>class A implements Persistable{</p>
 *  <p><B>@PrimaryKey </B></p>
 *  <p>private long id;</p>
 * <p><B>@ManyToMany </B></p>
 *  <p>private Collection < /B > bCollection;</p>
 * <p>}</p>
 * <p><B>@Entity </B></p>
 * <p>class B implements Persistable{</p>
 *  <p><B> @PrimaryKey </B></p>
 *   <p>private long id;</p>
 *  <p><B> @ManyToMany </B></p>
 *   <p>private Collection<A> aCollection;</p>
 * <p>}</p>
 * <p>which will create tables A,B and A_B_Cross in database</p>
 * @author Hamza Gumrah
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {
	/**
	 * if field is not map this attribute is underestimated,
	 * else it finds related field of target entity and writes field.value.toString 
	 * to map's key value.
	 * @return
	 */
	String mapKey() default OrmConstants.PRIMARY_KEY_FIELD_NAME;
	
	/**
	 * since it is not possible to get Collection<T> T's type during runtime
	 * type of the generic parameter should be written here. 
	 * @return parameterType e.g for a field List<EntityA> this should have EntityA.class
	 */
	Class<?> targetEntityClass();
}
