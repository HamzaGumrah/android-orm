package com.android.orm.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;
import com.android.orm.annotation.Column;
import com.android.orm.annotation.Entity;
import com.android.orm.annotation.ForeignKey;
import com.android.orm.exception.DublicatedEntityNameException;
import com.android.orm.exception.EntityNotFoundException;
import com.android.orm.exception.MultiplePrimaryKeyException;
import com.android.orm.exception.PersistableNotFoundException;
import com.android.orm.exception.PrimaryKeyNotFoundException;
import com.android.orm.exception.UnsupportedForeignKeyReferenceException;
import com.android.orm.exception.UnsupportedPrimaryKeyException;
import com.android.orm.util.PersistenceUtil;
import com.android.orm.util.ReflectionUtil;

/**
 * keeps meta data of Entities, Columns and Constrains. This class should only be used as SingleTone
 * 
 * @author Hamza Gumrah
 */
final class OrmRegistry {
	
	private final String TAG = "OrmRegistry";
	
	private final Map<String, EntityMetaData> registryMap;
	
	/**
	 * constructs Registry for entities
	 * 
	 * @param qualifiedEntityNames qualifiedNames of entities
	 * @throws ClassNotFoundException if qualifiedEntityName does not exists
	 * @throws NoSuchMethodException if getter or setter method is missing for any @Column field of the entity
	 * @throws SecurityException if getter or setter method is not public for any @Column field of the entity
	 */
	public OrmRegistry(Set<String> qualifiedEntityNames) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		this.registryMap = new HashMap<String, EntityMetaData>();
		for (String entityQualifiedName : qualifiedEntityNames) {
			Class<?> clazz = Class.forName(entityQualifiedName);
			// checks if target classes has Entity annotation
			if (!PersistenceUtil.isEntity(clazz))
				throw new EntityNotFoundException(entityQualifiedName);
			// checks if target classes implemented Persistable
			if (PersistenceUtil.isPersistable(clazz))
				throw new PersistableNotFoundException(entityQualifiedName);
			
			Entity e = clazz.getAnnotation(Entity.class);
			String entityName = e.name();
			if (e.name().equals("")) {
				Log.w(TAG, "Entities strongly couraged to have name(), else clazz.getSimpleName() will be mapped as tableName which can result further exceptions e.g DublicatedEntityName");
				entityName = clazz.getSimpleName();
			}
			if (this.registryMap.containsKey(entityName)) {
				throw new DublicatedEntityNameException(entityName);
			}
			EntityMetaData entityMetaData = constructEntityMetaData(clazz, entityName);
			this.registryMap.put(entityName, entityMetaData);
		}
	}
	
	/**
	 * helper method for constructor, finds declared fields of clazz and its superclasses. If fields have @Column,@ForeingKey,@ManyToMany e.t.c annotations adds them to metaData
	 * 
	 * @param clazz
	 * @return generated entityMetaData for clazz.
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	final EntityMetaData constructEntityMetaData(final Class<?> clazz, final String entityName) throws SecurityException, NoSuchMethodException {
		
		Class<?> type = clazz;
		EntityMetaData entityMetaData = new EntityMetaData();
		boolean hasPrimaryKey = false;
		while (Object.class.equals(type)) {
			Field[] fields = type.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(PrimaryKey.class)) {
					if (hasPrimaryKey)
						throw new MultiplePrimaryKeyException(entityName);
					else
						hasPrimaryKey = true;
					if (!Long.class.isAssignableFrom(field.getClass()))
						throw new UnsupportedPrimaryKeyException(field.getClass().getName());
					
					Method getMethod = ReflectionUtil.findGetMethod(clazz, OrmConstants.PRIMARY_KEY_FIELD_NAME);
					Method setMethod = ReflectionUtil.findSetMethod(clazz, Long.class, OrmConstants.PRIMARY_KEY_FIELD_NAME);
					entityMetaData.primaryKeyMetaData = new PrimaryKeyMetaData(field.getAnnotation(PrimaryKey.class).orderby(), getMethod, setMethod);
				}
				else if (field.isAnnotationPresent(Column.class)) {
					// check if fieldType supported
					PersistenceUtil.isFieldTypeSupported(field.getClass());
					Column column = field.getAnnotation(Column.class);
					
					String columnName = column.name();
					if (columnName.equals("")) {
						Log.w(TAG, "Columns strongly couraged to have name(), else fieldName will be mapped as column name ");
						columnName = field.getName();
					}
					// if Column is also a foreign key
					ForeignKeyMetaData foreignKeyMetaData = null;
					if (field.isAnnotationPresent(ForeignKey.class)) {
						if(PersistenceUtil.isPersistable(field.getClass())&&PersistenceUtil.isEntity(field.getClass()))
							;
						else
							Log.w(TAG, entityName+" ForeignKey field is not Persistable or Entity underestimating ... ");
						
						String foreignKeyReference = field.getAnnotation(ForeignKey.class).reference();
						if (!foreignKeyReference.equals(OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE)) {
							Class<?> referenceType = ReflectionUtil.findFieldType(field.getClass(), foreignKeyReference);
							if (!PersistenceUtil.isForeignKeyReferenceSupported(referenceType))
								throw new UnsupportedForeignKeyReferenceException(entityName, field.getName(), foreignKeyReference, referenceType.getName());
							foreignKeyMetaData = new ForeignKeyMetaData(PersistenceUtil.getEntityName(field.getClass()), columnName, foreignKeyReference, referenceType);
						}
						else
							foreignKeyMetaData = new ForeignKeyMetaData(PersistenceUtil.getEntityName(field.getClass()));
					}
					
					Method getMethod = ReflectionUtil.findGetMethod(clazz, field.getName());
					Method setMethod = ReflectionUtil.findSetMethod(clazz, field.getClass(), field.getName());
					entityMetaData.put(columnName, new ColumnMetaData(column, getMethod, setMethod, foreignKeyMetaData, field.getClass()));
				}
			}
		}
		for (Field field : fields) {
			// if a field has PrimaryKey annotation override its @Column annotation
			if (field.isAnnotationPresent(PrimaryKey.class)) {
				if (hasPrimaryKey)
					throw new MultiplePrimaryKeyException(entityName);
				else
					hasPrimaryKey = true;
				if (!Long.class.isAssignableFrom(field.getClass()))
					throw new UnsupportedPrimaryKeyException(field.getClass().getName());
				
				Method getMethod = ReflectionUtil.findGetMethod(clazz, OrmConstants.PRIMARY_KEY_FIELD_NAME);
				Method setMethod = ReflectionUtil.findSetMethod(clazz, Long.class, OrmConstants.PRIMARY_KEY_FIELD_NAME);
				entityMetaData.primaryKeyMetaData = new PrimaryKeyMetaData(field.getAnnotation(PrimaryKey.class).orderby(), getMethod, setMethod);
			}
			else if (field.isAnnotationPresent(Column.class)) {
				// check if fieldType supported
				PersistenceUtil.isFieldTypeSupported(field.getClass());
				Column column = field.getAnnotation(Column.class);
				
				String columnName = column.name();
				if (columnName.equals("")) {
					Log.w(TAG, "Columns strongly couraged to have name(), else fieldName will be mapped as column name ");
					columnName = field.getName();
				}
				// if Column is also a foreign key
				ForeignKeyMetaData foreignKeyMetaData = null;
				if (field.isAnnotationPresent(ForeignKey.class)) {
					PersistenceUtil.isPersistable(field.getClass());
					PersistenceUtil.isEntity(field.getClass());
					
					String foreignKeyReference = field.getAnnotation(ForeignKey.class).reference();
					if (!foreignKeyReference.equals(OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE)) {
						Class<?> referenceType = ReflectionUtil.findFieldType(field.getClass(), foreignKeyReference);
						if (!PersistenceUtil.isForeignKeyReferenceSupported(referenceType))
							throw new UnsupportedForeignKeyReferenceException(entityName, field.getName(), foreignKeyReference, referenceType.getName());
						foreignKeyMetaData = new ForeignKeyMetaData(PersistenceUtil.getEntityName(field.getClass()), columnName, foreignKeyReference, referenceType);
					}
					else
						foreignKeyMetaData = new ForeignKeyMetaData(PersistenceUtil.getEntityName(field.getClass()));
				}
				
				Method getMethod = ReflectionUtil.findGetMethod(clazz, field.getName());
				Method setMethod = ReflectionUtil.findSetMethod(clazz, field.getClass(), field.getName());
				entityMetaData.put(columnName, new ColumnMetaData(column, getMethod, setMethod, foreignKeyMetaData, field.getClass()));
			}
		}
		if (!hasPrimaryKey)
			throw new PrimaryKeyNotFoundException(entityName);
		return entityMetaData;
	}
	
	final Map<String, EntityMetaData> getRegistryData() {
		return this.registryMap;
	}
	
	final EntityMetaData getEntityMetaData(String entityName) {
		return this.registryMap.get(entityName);
	}
	
}
