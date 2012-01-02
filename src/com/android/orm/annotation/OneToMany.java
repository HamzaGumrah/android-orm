/**
 * 
 */
package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.android.orm.OrmConstants;



/**
 * field which has this annotation should be another Persistable Entity
 * this annotation is not allowed to be used together with ForeignKey and Column
 * e.g.
 * <p> <B>@Entity </B></p>
 * <p>class A{</p>
 * <p> <B>@PrimaryKey </B></p>
 * <p> private long id;</p>
 * <p> <B>@OneToMany </B></p>
 *  <p>private Collection < /B > bCollection;</p>
 *<p>}</p>
 *  
 * <p><B>@Entity </B></p>
 * <p>class B{</p>
 * <p><B>@PrimaryKey </B></p>
 * <p>private long id;</p>
 * <p><B>@ForeignKey </B></p>
 * <p>private A a;</p>
 * <p>}
 * 
 * <p><B>@Entity </B></p>
 * <p>class C{</p>
 *  <p><B>@PrimaryKey </B></p>
 *  <p>private long id;</p>
 *  <p><B>@OneToMany </B> </p>
 * <p> private Map<String,B> bMap; //this will map B.id.toString() to key value</p>
 * <p>}</p>
 * @author Hamza Gumrah
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {
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
