package com.android.orm.util;

import com.android.orm.annotation.Entity;
import com.android.orm.exception.EntityNotFoundException;
import com.android.orm.exception.PersistableNotFoundException;

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
}
