package com.android.orm.adapter;

import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;
import com.android.orm.annotation.Entity;
import com.android.orm.exception.CircularReferenceException;
import com.android.orm.exception.ColumnNotNullableException;
import com.android.orm.exception.UnRegisteredEntityException;
import com.android.orm.exception.UnsupportedFieldTypeException;
import com.android.orm.util.PersistenceUtil;
import com.android.orm.util.ReflectionUtil;

/**
 * contains static methods to use registry information
 * 
 * @author Hamza Gumrah
 */
abstract class SqliteHelper {
	
	/**
	 * Integer,Long,Short,Byte,Boolean,Date are recorded as INTEGER String and Enum types are recorded as TEXT Double and Float types are recorded as REAL byte[] is BLOB
	 * 
	 * @return sqlite column type's name in uppercase
	 */
	static final String getSqliteTypeName(Class<?> clazz) {
		
		if (Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)
				|| Short.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)
				|| Boolean.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_NUMBER;
		if (String.class.isAssignableFrom(clazz) || Enum.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_TEXT;
		if (Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_REAL;
		if (byte[].class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_BLOB;
		if (Persistable.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_NUMBER;
		return "";
	}
	
	// ###########################################################################################################
	// GENERATE STATEMENTS FOR DATABASE INITIALIZATION
	// Creating Create Table statements is essential, Sqlite ALTER TABLE can not add a foreign key constraint, hence we need to define each foreign key inside CREATE TABLE statement
	// Therefore, CRATE TABLE statements should be in a correct order.
	// ###########################################################################################################
	/**
	 * @return generated create statements for database
	 */
	static final Set<String> generateCreateStatements() {
		
		Set<String> statements = new HashSet<String>();
		// set foreign keys off at first
		statements.add("PRAGMA foreign_keys = OFF;");
		// keeps entity names which already have create statements
		Set<String> generatedEntities = new HashSet<String>();
		// first create statements for non dependent entities which does not have any foreign key.
		boolean hasNonDependentEntity = false;
		for (String entityName : DatabaseAdapterFactory.getRegistryData().keySet()) {
			EntityMetaData entityMetaData = DatabaseAdapterFactory.getEntityMetaData(entityName);
			// if this is a dependent entity continue
			if (entityMetaData.hasDependetEntity())
				continue;
			hasNonDependentEntity = true;
			StringBuilder sqlBuilder = new StringBuilder();
			addCreateStatementHeader(sqlBuilder, entityName);
			for (String columnName : entityMetaData.columnNames()) {
				ColumnMetaData columnMetaData = entityMetaData.columns.get(columnName);
				sqlBuilder.append(", ");
				sqlBuilder.append(columnName);
				sqlBuilder.append(" " + columnMetaData.getSqliteFieldType());
				// if length value is specified for Text attr
				if (columnMetaData.getSqliteFieldType().equals(OrmConstants.SQLITE_TEXT) && columnMetaData.length() > 0)
					sqlBuilder.append("(" + columnMetaData.length() + ")");
			}
			sqlBuilder.append(", PRIMARY KEY(");
			sqlBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
			sqlBuilder.append(" " + entityMetaData.primaryKeyMetaData.getOrderType() + ")");
			sqlBuilder.append(");");
			statements.add(sqlBuilder.toString());
			generatedEntities.add(entityName);
		}
		// if all entities have foreing keys than its not possible to create tables
		if (!hasNonDependentEntity)
			throw new CircularReferenceException("All entities has dependency , required at least 1 entity without foreign key");
		// create statements for dependent entities
		for (String entityName : DatabaseAdapterFactory.getRegistryData().keySet()) {
			EntityMetaData entityMetaData = DatabaseAdapterFactory.getEntityMetaData(entityName);
			if (!entityMetaData.hasDependetEntity() || generatedEntities.contains(entityName))
				continue;
			generateCreateStatement(entityName, statements, generatedEntities);
		}
		statements.add("PRAGMA foreign_keys = ON;");
		return statements;
	}
	
	/**
	 * recursive method , generates create statements for an entity and its dependent entities.
	 * 
	 * @param entityName
	 * @param statements
	 * @param generatedEntities
	 * @param stack required this arg to detect circular references
	 */
	private static final void generateCreateStatement(final String entityName, final Set<String> statements, final Set<String> generatedEntities) {
		EntityMetaData entityMetaData = DatabaseAdapterFactory.getEntityMetaData(entityName);
		if (entityMetaData == null)
			throw new UnRegisteredEntityException(entityName);
		// first generate create statements for dependentEntities
		for (ForeignKeyMetaData foreignKeyMetaData : entityMetaData.dependentEntities.values()) {
			// if create statement already generated for dependent entity skip it
			if (generatedEntities.contains(foreignKeyMetaData.getReferenceEntityName()))
				continue;
			// if create statement not generated , generate its statement before this one.
			generateCreateStatement(foreignKeyMetaData.getReferenceEntityName(), statements, generatedEntities);
		}
		// after generating create statements for all dependents , we can continue for this entity
		StringBuilder sqlBuilder = new StringBuilder();
		addCreateStatementHeader(sqlBuilder, entityName);
		for (String columnName : entityMetaData.columnNames()) {
			ColumnMetaData columnMetaData = entityMetaData.columns.get(columnName);
			sqlBuilder.append(", ");
			sqlBuilder.append(columnName);
			sqlBuilder.append(" " + columnMetaData.getSqliteFieldType());
			// if length value is specified for Text attr
			if (columnMetaData.getSqliteFieldType().equals(OrmConstants.SQLITE_TEXT) && columnMetaData.length() > 0)
				sqlBuilder.append("(" + columnMetaData.length() + ")");
		}
		// Primary Key definition
		sqlBuilder.append(", PRIMARY KEY(");
		sqlBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
		sqlBuilder.append(" " + entityMetaData.getPrimaryKeyMetaData().getOrderType() + ")");
		// foreign key definitions
		for (String columnName : entityMetaData.getDependentEntities().keySet()) {
			sqlBuilder.append(", FOREIGN KEY(");
			sqlBuilder.append(columnName);
			sqlBuilder.append(") REFERENCES ");
			ForeignKeyMetaData foreignKeyMetaData = entityMetaData.getDependentEntities().get(columnName);
			sqlBuilder.append(foreignKeyMetaData.getReferenceEntityName());
			sqlBuilder.append("(" + foreignKeyMetaData.getReferenceColumnName() + ")");
		}
		
		sqlBuilder.append(");");
		generatedEntities.add(entityName);
		statements.add(sqlBuilder.toString());
	}
	
	/**
	 * adds a must have header to each create statement
	 * 
	 * @param sqlBuilder
	 * @param entityName
	 */
	private static final void addCreateStatementHeader(final StringBuilder sqlBuilder, final String entityName) {
		sqlBuilder.append("CREATE TABLE ");
		sqlBuilder.append(entityName.toUpperCase());
		sqlBuilder.append("(");
		sqlBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
		sqlBuilder.append(" INTEGER");
	}
	
	// ###########################################################################################################
	// END OF GENERATE STATEMENTS FOR DATABASE INITIALIZATION
	// ###########################################################################################################
	
	// ###########################################################################################################
	// GET CONTENT VALUES FOR SQLITEDATABASE INSERT
	// ###########################################################################################################
	/**
	 * @param entity
	 * @return ContentValues which will be used in SqliteDatabase.insert method
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	static final ContentValues getContentValues(final Persistable obj) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		
		String entityName = PersistenceUtil.getEntityName(obj.getClass());
		
		EntityMetaData entityMetaData = DatabaseAdapterFactory.getEntityMetaData(entityName);
		if (entityMetaData == null)
			throw new UnRegisteredEntityException(entityName);
		
		ContentValues values = new ContentValues();
		for (String columnName : entityMetaData.columns.keySet())
			addToContent(values, columnName, entityMetaData.columnGetter(columnName).invoke(obj), entityMetaData.columns.get(columnName));
		
		return values;
	}
	
	/**
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	private static final void addToContent(final ContentValues values, final String columnName, final Object value, final ColumnMetaData columnMetaData) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (value == null && !columnMetaData.isNullable())
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
			if (columnMetaData.isForeignKey()
					&& !columnMetaData.getForeignKeyMetaData().getReferenceFieldName().equals(OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE)) {
				addToContent(values, columnName, ReflectionUtil.invokeGetMethod(value, columnMetaData.getForeignKeyMetaData().getReferenceFieldName(), columnMetaData.getForeignKeyMetaData().getReferenceFieldType()), columnMetaData);
			}
			else
				values.put(columnName, ((Persistable) value).getId());
		}
		else
			throw new UnsupportedFieldTypeException(value.getClass().getName());
	}
	
	// ###########################################################################################################
	// END OF GET CONTENT VALUES FOR SQLITEDATABASE INSERT
	// ###########################################################################################################
	
	/**
	 * generates DROP TABLE statements
	 */
	static final Set<String> generateDropStatements() {
		Set<String> entities = DatabaseAdapterFactory.getRegistryData().keySet();
		Set<String> statements = new HashSet<String>();
		for (String entity : entities)
			statements.add("DROP TABLE IF EXISTS " + entity);
		return statements;
	}
	/**
	 * 
	 * @param clazz
	 * @return if class can be mapped to database table or not. 
	 */
	static final boolean mappable(Class<?> clazz) {
		return PersistenceUtil.isEntity(clazz) && PersistenceUtil.isPersistable(clazz);
	}
	
	// /**
	// * gets value of the column. If entity is not registered throws @see UnRegisteredEntityException
	// *
	// * @param entity
	// * @param columnName
	// * @return
	// * @throws IllegalArgumentException
	// * @throws IllegalAccessException
	// * @throws InvocationTargetException
	// */
	// public final Object getValueOfColumn(final Entity entity, final String columnName) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
	// String key = entity.name();
	// if (key.equals(""))
	// key = entity.getClass().getSimpleName();
	// EntityMetaData entityMetaData = this.registryMap.get(key);
	// if (entityMetaData == null)
	// throw new UnRegisteredEntityException(entity.name());
	// return entityMetaData.columnGetter(columnName).invoke(entity);
	// }
	//
	// /**
	// * @param qualifiedName
	// * @param fieldName
	// */
	// public final void setValueOf(final String entityName, final String columnName, final Object value) {
	//
	// }
	//
	// /**
	// * gets values of each field
	// *
	// * @return <column.name(),value> map.
	// * @throws InvocationTargetException
	// * @throws IllegalAccessException
	// * @throws IllegalArgumentException
	// */
	// public final Map<String, Object> getValues(final Persistable obj) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
	// String entityName = PersistenceUtil.getEntityName(obj.getClass());
	// EntityMetaData metaData = this.registryMap.get(entityName);
	// if (metaData == null)
	// throw new UnRegisteredEntityException(entityName);
	// Map<String, Object> result = new HashMap<String, Object>();
	// for (String columnName : metaData.columns.keySet())
	// result.put(columnName, metaData.columnGetter(columnName).invoke(obj));
	// return result;
	// }
}
