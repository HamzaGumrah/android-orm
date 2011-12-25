package com.android.orm.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;
import com.android.orm.annotation.Column;
import com.android.orm.annotation.Entity;
import com.android.orm.annotation.ForeignKey;
import com.android.orm.annotation.PrimaryKey;
import com.android.orm.exception.ColumnNotNullableException;
import com.android.orm.exception.DublicatedEntityNameException;
import com.android.orm.exception.EntityNotFoundException;
import com.android.orm.exception.MultiplePrimaryKeyException;
import com.android.orm.exception.PrimaryKeyNotFoundException;
import com.android.orm.exception.UnRegisteredEntityException;
import com.android.orm.exception.UnsupportedFieldTypeException;
import com.android.orm.exception.UnsupportedForeignKeyReferenceException;
import com.android.orm.exception.UnsupportedPrimaryKeyTypeException;
import com.android.orm.type.OrderType;
import com.android.orm.util.PersistenceUtil;
import com.android.orm.util.ReflectionUtil;
import com.android.orm.util.SqliteUtil;

/**
 * keeps related getter and setter methods of fields. Also checks if entity classes are well-defined or not. Initializes at SqliteAdapter's constructor. this registry designed for reaching
 * getters,setters method faster than usual Reflection approach.
 * 
 * @author Hamza Gumrah
 */
final class OrmRegistry {
	
	private final String TAG = "OrmRegistry";
	
	/**
	 * keeps {<entity name> : {<column name>:<column registry>}}
	 */
	private final Map<String, Map<String, ColumnRegistry>> registryMap;
	
	/**
	 * constructs Registry for entities
	 * 
	 * @param qualifiedEntityNames qualifiedNames of entities
	 * @throws ClassNotFoundException if qualifiedEntityName does not exists
	 * @throws NoSuchMethodException if getter or setter method is missing for any @Column field of the entity
	 * @throws SecurityException if getter or setter method is not public for any @Column field of the entity
	 */
	public OrmRegistry(String... qualifiedEntityNames) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		this.registryMap = new HashMap<String, Map<String, ColumnRegistry>>();
		for (String entityQualifiedName : qualifiedEntityNames) {
			Class<?> clazz = Class.forName(entityQualifiedName);
			// checks if target classes has Entity annotation
			PersistenceUtil.isEntity(clazz);
			// checks if target classes implemented Persistable
			PersistenceUtil.isPersistable(clazz);
			Entity e = clazz.getAnnotation(Entity.class);
			String entityName = e.name();
			if (e.name().equals("")) {
				Log.w(TAG, "Entities strongly couraged to have name(), else clazz.getSimpleName() will be mapped as tableName which can result further exceptions e.g DublicatedEntityName");
				entityName = clazz.getSimpleName();
			}
			if (this.registryMap.containsKey(entityName)) {
				throw new DublicatedEntityNameException(entityName);
			}
			Field[] fields = clazz.getDeclaredFields();
			Map<String, ColumnRegistry> entityMap = new HashMap<String, OrmRegistry.ColumnRegistry>();
			boolean hasPrimaryKey = false;
			for (Field field : fields) {
				// if a field has PrimaryKey annotation override its @Column annotation
				if (field.isAnnotationPresent(PrimaryKey.class)) {
					if (hasPrimaryKey)
						throw new MultiplePrimaryKeyException(entityName);
					else
						hasPrimaryKey = true;
					if (!Long.class.isAssignableFrom(field.getClass()))
						throw new UnsupportedPrimaryKeyTypeException(field.getClass().getName());
					
					Method getMethod = ReflectionUtil.findGetMethod(clazz, OrmConstants.PRIMARY_KEY_FIELD_NAME);
					Method setMethod = ReflectionUtil.findSetMethod(clazz, Long.class, OrmConstants.PRIMARY_KEY_FIELD_NAME);
					PrimaryKeyMetaData primaryKeyMetaData = new PrimaryKeyMetaData(field.getAnnotation(PrimaryKey.class).orderby());
					entityMap.put(OrmConstants.PRIMARY_KEY_COLUMN_NAME, new ColumnRegistry(primaryKeyMetaData, getMethod, setMethod));
				}
				else if (field.isAnnotationPresent(Column.class)) {
					// check if fieldType supported
					PersistenceUtil.isFieldTypeSupported(field.getClass());
					Column column = field.getAnnotation(Column.class);
					// if Column is also a foreign key
					ForeignKeyMetaData foreignKeyMetaData = null;
					if (field.isAnnotationPresent(ForeignKey.class)) {
						PersistenceUtil.isEntity(field.getClass());
						PersistenceUtil.isPersistable(field.getClass());
						String foreignKeyReference = field.getAnnotation(ForeignKey.class).reference();
						if (!foreignKeyReference.equals(OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE)) {
							Class<?> referenceType = ReflectionUtil.findFieldType(field.getClass(), foreignKeyReference);
							if (!PersistenceUtil.isForeignKeyReferenceSupported(referenceType))
								throw new UnsupportedForeignKeyReferenceException(entityName, field.getName(), foreignKeyReference, referenceType.getName());
							foreignKeyMetaData = new ForeignKeyMetaData(foreignKeyReference, referenceType);
						}
						else {
							foreignKeyMetaData = new ForeignKeyMetaData(foreignKeyReference);
						}
					}
					
					Method getMethod = ReflectionUtil.findGetMethod(clazz, field.getName());
					Method setMethod = ReflectionUtil.findSetMethod(clazz, field.getClass(), field.getName());
					if (column.name().equals("")) {
						Log.w(TAG, "Columns strongly couraged to have name(), else fieldName will be mapped as column name ");
						entityMap.put(field.getName(), new ColumnRegistry(column, getMethod, setMethod, foreignKeyMetaData, field.getClass()));
					}
					else
						entityMap.put(column.name(), new ColumnRegistry(column, getMethod, setMethod, foreignKeyMetaData, field.getClass()));
					
				}
			}
			if (!hasPrimaryKey)
				throw new PrimaryKeyNotFoundException(entityName);
			this.registryMap.put(entityName, entityMap);
		}
	}
	
	/**
	 * gets value of the column. If entity is not registered throws @see UnRegisteredEntityException
	 * 
	 * @param entity
	 * @param columnName
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public final Object getValueOfColumn(final Entity entity, final String columnName) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String key = entity.name();
		if (key.equals(""))
			key = entity.getClass().getSimpleName();
		Map<String, ColumnRegistry> entityMap = this.registryMap.get(key);
		if (entityMap == null)
			throw new UnRegisteredEntityException(entity.name());
		return entityMap.get(columnName).getMethod.invoke(entity);
	}
	
	/**
	 * @param qualifiedName
	 * @param fieldName
	 */
	public final void setValueOf(final String entityName, final String columnName, final Object value) {
		
	}
	
	/**
	 * @return generated create statements for database
	 */
	public final String[] generateCreateStatements() {
		List<String> statements = new ArrayList<String>();
		// set foreign keys off at first
		statements.add("PRAGMA foreign_keys = OFF;");
		for (String entityName : this.registryMap.keySet()) {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("CREATE TABLE ");
			queryBuilder.append(entityName.toUpperCase());
			// start filling column definitions
			queryBuilder.append("(");
			queryBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
			queryBuilder.append(" INTEGER");
			OrderType primaryKeyOrderType = OrderType.DESC;
			Map<String, ColumnRegistry> columnMetaData = this.registryMap.get(entityName);
			for (String columnName : columnMetaData.keySet()) {
				ColumnRegistry columnRegistry = columnMetaData.get(columnName);
				if (columnRegistry.isPrimaryKey()) {
					primaryKeyOrderType = columnRegistry.primaryKeyMetaData.orderby;
					continue;
				}
				queryBuilder.append(", ");
				queryBuilder.append(columnName);
				queryBuilder.append(" " + columnRegistry.sqliteFieldType);
				if (columnRegistry.sqliteFieldType.equals(OrmConstants.SQLITE_TEXT) && columnRegistry.self.length() > 0)
					queryBuilder.append("(" + columnRegistry.self.length() + ")");
			}
			queryBuilder.append(", PRIMARY KEY(");
			queryBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
			queryBuilder.append(" " + primaryKeyOrderType.name() + ")");
			queryBuilder.append(");");
			// end column definitions
		}
		statements.add("PRAGMA foreign_keys = ON;");
		return statements.toArray(new String[0]);
	}
	
	/**
	 * gets values of each field
	 * 
	 * @return <column.name(),value> map.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public final Map<String, Object> getValues(final Persistable obj) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (!obj.getClass().isAnnotationPresent(Entity.class))
			throw new EntityNotFoundException(obj.getClass().getName());
		Entity e = obj.getClass().getAnnotation(Entity.class);
		String key = e.name();
		if (e.name().equals(""))
			key = obj.getClass().getSimpleName();
		Map<String, ColumnRegistry> entityMap = this.registryMap.get(key);
		if (entityMap == null)
			throw new EntityNotFoundException(obj.getClass().getName());
		Map<String, Object> result = new HashMap<String, Object>();
		for (String columnName : entityMap.keySet())
			result.put(columnName, entityMap.get(columnName).getMethod.invoke(obj));
		
		return result;
	}
	
	/**
	 * @param entity
	 * @return ContentValues which will be used in SqliteDatabase.insert method
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public final ContentValues getContentValues(final Persistable obj) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		PersistenceUtil.isEntity(obj.getClass());
		String entityName = PersistenceUtil.getEntityName(obj.getClass());
		
		Map<String, ColumnRegistry> entityMap = this.registryMap.get(entityName);
		if (entityMap == null)
			throw new UnRegisteredEntityException(entityName);
		
		ContentValues values = new ContentValues();
		for (String columnName : entityMap.keySet()) {
			ColumnRegistry columnRegistry = entityMap.get(columnName);
			Object value = columnRegistry.getMethod.invoke(obj);
			
			this.addToContent(values, columnName, value, columnRegistry);
			
		}
		return values;
	}
	
	/**
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	private final void addToContent(final ContentValues values, final String columnName, final Object value, final ColumnRegistry columnRegistry) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (value == null && !columnRegistry.isNullable())
			throw new ColumnNotNullableException(columnName);
		if (value == null)
			return;
		else if (value instanceof String)
			values.put(columnName, (String) value);
		else if (value instanceof Long)
			values.put(columnName, (Long) value);
		else if (value instanceof Integer)
			values.put(columnName, (Integer) value);
		else if (value instanceof Short)
			values.put(columnName, (Short) value);
		else if (value instanceof Byte)
			values.put(columnName, (Byte) value);
		else if (value instanceof Boolean)
			values.put(columnName, (Boolean) value);
		else if (value instanceof Double)
			values.put(columnName, (Double) value);
		else if (value instanceof Float)
			values.put(columnName, (Float) value);
		else if (value instanceof byte[])
			values.put(columnName, (byte[]) value);
		else if (Enum.class.isAssignableFrom(value.getClass()))
			values.put(columnName, Enum.class.cast(value).name());
		else if (value instanceof Persistable) {
			if (columnRegistry.foreignKeyMetaData != null && !columnRegistry.foreignKeyMetaData.referenceFieldName.equals(OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE)) {
				this.addToContent(values, columnName, ReflectionUtil.invokeGetMethod(value, columnRegistry.foreignKeyMetaData.referenceFieldName, columnRegistry.foreignKeyMetaData.referenceFieldType), columnRegistry);
			}
			else
				values.put(columnName, ((Persistable) value).getId());
		}
		else
			throw new UnsupportedFieldTypeException(value.getClass().getName());
	}
	
	private final class ColumnRegistry {
		
		private final Column self;
		
		private final String sqliteFieldType;
		
		private final Method getMethod;
		
		private final Method setMethod;
		
		private final PrimaryKeyMetaData primaryKeyMetaData;
		
		private final ForeignKeyMetaData foreignKeyMetaData;
		
		private final boolean isDate;
		
		ColumnRegistry(PrimaryKeyMetaData primaryKeyMetaData, Method getMethod, Method setMethod) {
			this.primaryKeyMetaData = primaryKeyMetaData;
			this.getMethod = getMethod;
			this.setMethod = setMethod;
			this.foreignKeyMetaData = null;
			this.self = null;
			this.isDate = false;
			this.sqliteFieldType = OrmConstants.SQLITE_NUMBER;
		}
		
		ColumnRegistry(Column self, Method getMethod, Method setMethod, ForeignKeyMetaData foreignKeyMetaData, Class<?> fieldType) {
			super();
			this.primaryKeyMetaData = null;
			this.isDate = PersistenceUtil.isDate(fieldType);
			this.self = self;
			this.getMethod = getMethod;
			this.setMethod = setMethod;
			this.foreignKeyMetaData = foreignKeyMetaData;
			// if this is a foreign key , sqliteFieldType should be foreign key reference column type
			if (this.foreignKeyMetaData != null)
				this.sqliteFieldType = SqliteUtil.getSqliteTypeName(this.foreignKeyMetaData.referenceFieldType);
			else
				this.sqliteFieldType = SqliteUtil.getSqliteTypeName(fieldType);
		}
		
		private final boolean isNullable() {
			if (this.primaryKeyMetaData != null)
				return false;
			return self.nullable();
		}
		
		private final boolean isPrimaryKey() {
			return this.primaryKeyMetaData != null;
		}
		
		private final boolean isForeignKey() {
			return this.foreignKeyMetaData != null;
		}
	}
	
	private final class ForeignKeyMetaData {
		
		private final String referenceFieldName;
		
		private final Class<?> referenceFieldType;
		
		ForeignKeyMetaData(String referenceFieldName) {
			this(referenceFieldName, Long.class);
		}
		
		ForeignKeyMetaData(String referenceFieldName, Class<?> referenceFieldType) {
			super();
			if (referenceFieldName != null && referenceFieldType != null) {
				this.referenceFieldName = referenceFieldName;
				this.referenceFieldType = referenceFieldType;
			}
			else {
				this.referenceFieldName = OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE;
				this.referenceFieldType = Long.class;
			}
		}
		
	}
	
	private final class PrimaryKeyMetaData {
		
		private final OrderType orderby;
		
		PrimaryKeyMetaData(OrderType orderby) {
			this.orderby = orderby;
		}
	}
}
