package com.android.orm.util;

import java.sql.Date;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;
import com.android.orm.annotation.Entity;
import com.android.orm.exception.EntityViolation;
import com.android.orm.exception.UnsupportedFieldTypeException;

public abstract class PersistenceUtil {
	
	/**
	 * checks if class has Entity Annotation
	 * 
	 * @throws EntityViolation if class is abstract and defined as entity
	 */
	public static boolean isEntity(Class<?> clazz) {
		if (!clazz.isAnnotationPresent(Entity.class))
			return false;
		return true;
	}
	
	/**
	 * @param obj
	 * @return if entity is persisted to database or not.
	 */
	public static boolean isPersisted(Persistable obj) {
		if (obj.getId() == OrmConstants.NOT_PERSISTED_ID)
			return false;
		return true;
	}
	
	
	/**
	 * checks if Persistable.class.isAssignableFrom(clazz)
	 */
	public static boolean isPersistable(Class<?> clazz) {
		return Persistable.class.isAssignableFrom(clazz);
	}
	
	public static boolean isDate(Class<?> clazz) {
		return java.util.Date.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz);
	}
	
	/**
	 * checks if fieldType supported or not
	 * 
	 * @param fieldType
	 */
	public static void isFieldTypeSupported(final Class<?> fieldType) {
		if (Integer.class.isAssignableFrom(fieldType))
			return;
		if (Long.class.isAssignableFrom(fieldType))
			return;
		if (String.class.isAssignableFrom(fieldType))
			return;
		if (Byte.class.isAssignableFrom(fieldType))
			return;
		if (Boolean.class.isAssignableFrom(fieldType))
			return;
		if (byte[].class.isAssignableFrom(fieldType))
			return;
		if (Short.class.isAssignableFrom(fieldType))
			return;
		if (Double.class.isAssignableFrom(fieldType))
			return;
		if (Float.class.isAssignableFrom(fieldType))
			return;
		if (Enum.class.isAssignableFrom(fieldType))
			return;
		if (isPersistable(fieldType) && isEntity(fieldType))
			return;
		if (isDate(fieldType))
			return;
		throw new UnsupportedFieldTypeException(fieldType.getName());
	}
	
	public static boolean isForeignKeyReferenceSupported(final Class<?> referenceType) {
		if (Integer.class.isAssignableFrom(referenceType))
			return true;
		if (Long.class.isAssignableFrom(referenceType))
			return true;
		if (String.class.isAssignableFrom(referenceType))
			return true;
		if (Byte.class.isAssignableFrom(referenceType))
			return true;
		if (Boolean.class.isAssignableFrom(referenceType))
			return true;
		if (byte[].class.isAssignableFrom(referenceType))
			return true;
		if (Short.class.isAssignableFrom(referenceType))
			return true;
		if (Double.class.isAssignableFrom(referenceType))
			return true;
		if (Float.class.isAssignableFrom(referenceType))
			return true;
		if (Enum.class.isAssignableFrom(referenceType))
			return true;
		if (Persistable.class.isAssignableFrom(referenceType))
			return true;
		return false;
	}
}
