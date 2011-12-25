package com.android.orm.util;

import java.sql.Date;

import com.android.orm.Persistable;
import com.android.orm.annotation.Entity;
import com.android.orm.exception.EntityNotFoundException;
import com.android.orm.exception.PersistableNotFoundException;
import com.android.orm.exception.UnsupportedFieldTypeException;

public abstract class PersistenceUtil {
	
	/**
	 * checks if class has Entity Annotation
	 * 
	 * @throws EntityNotFoundException
	 */
	public static void isEntity(Class<?> clazz) {
		if (!clazz.isAnnotationPresent(Entity.class))
			throw new EntityNotFoundException(clazz.getName());
	}
	
	/**
	 * @param clazz
	 * @return gets Entity Annotation of the clazz and returns its name() if exists else return clazz.getSimpleName()
	 */
	public static String getEntityName(Class<?> clazz) {
		try {
			Entity e = clazz.getAnnotation(Entity.class);
			if (e.name().equals(""))
				return clazz.getSimpleName();
			else
				return e.name();
		}
		catch (Exception ex) {
			throw new EntityNotFoundException(clazz.getSimpleName());
		}
	}
	
	/**
	 * checks if Persistable.class.isAssignableFrom(clazz)
	 * 
	 * @throws PersistableNotFoundException
	 */
	public static void isPersistable(Class<?> clazz) {
		if (!clazz.isAnnotationPresent(Entity.class))
			throw new PersistableNotFoundException(clazz.getName());
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
		if (Persistable.class.isAssignableFrom(fieldType))
			return;
		if (Date.class.isAssignableFrom(fieldType))
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
