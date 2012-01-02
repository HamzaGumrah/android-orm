package com.android.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.android.orm.OrmConstants;
import com.android.orm.type.FetchType;
/**
 * ForeignKey's should also have @Column annotation, else they will be underestimated
 *<p> e.g.</p>
 * <p> @Entity </B></p>
 * <p>class A implements Persistable{</p>
 *  <p> @PrimaryKey </B></p>
 *  <p>private long id;</p>
 *  <B><p> @ForeignKey </B>(reference = "identity")</p>
 *  <B><p>@Column </B>(name="b_identity")</p>
 *  <p>private B b;</p>
 * <p>}</p>
 * 
 * <p> @Entity </B></p>
 * <p>class B implements Persistable{</p>
 * <p> @PrimaryKey </B></p>
 *  <p>private long id;</p>
 * <p> @Column </B></p>
 *  <p>private String identity;</p>
 * <p>}</p>
 * @author Hamza
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
	
	/**
	 * @return reference fieldName. references should be primitive types, String or Enum , primary keys strongly suggested
	 */
	String reference() default OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE;
	
	FetchType fetch() default FetchType.EAGER;
}
