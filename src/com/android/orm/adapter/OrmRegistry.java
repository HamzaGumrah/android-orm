package com.android.orm.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.util.Log;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;
import com.android.orm.annotation.Column;
import com.android.orm.annotation.Entity;
import com.android.orm.annotation.ForeignKey;
import com.android.orm.annotation.PrimaryKey;
import com.android.orm.exception.CircularReferenceException;
import com.android.orm.exception.ColumnNotNullableException;
import com.android.orm.exception.DublicatedEntityNameException;
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
	public OrmRegistry(String... qualifiedEntityNames) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		this.registryMap = new HashMap<String, EntityMetaData>();
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
			EntityMetaData entityMetaData = new EntityMetaData();
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
			this.registryMap.put(entityName, entityMetaData);
		}
	}
	
	// ###########################################################################################################
	// END OF CONSTRUCTOR
	// ###########################################################################################################
	
	// ###########################################################################################################
	// GENERATE STATEMENTS FOR DATABASE INITIALIZATION
	// Creating Create Table statements is essential, Sqlite ALTER TABLE can not add a foreign key constraint, hence we need to define each foreign key inside CREATE TABLE statement
	// Therefore, CRATE TABLE statements should be in a correct order.
	// ###########################################################################################################
	/**
	 * @return generated create statements for database
	 */
	public final String[] generateCreateStatements() {
		Collection<String> statements = new ArrayList<String>();
		// set foreign keys off at first
		statements.add("PRAGMA foreign_keys = OFF;");
		// first generate create table statements for entities which do not have any foreign key.
		Set<String> generatedEntities = this.generateCreateStatementsForNondependents(statements);
		if (generatedEntities.size() == 0)
			throw new CircularReferenceException("All entities has dependency , required at least 1 entity without foreign key");
		
		this.generateCreateStatementsForDependents(statements, generatedEntities);
		statements.add("PRAGMA foreign_keys = ON;");
		return statements.toArray(new String[0]);
	}
	
	/**
	 * add create table statement for entities which do not depent on another entity
	 * 
	 * @param statements
	 */
	private final Set<String> generateCreateStatementsForNondependents(final Collection<String> statements) {
		Set<String> result = new HashSet<String>();
		for (String entityName : this.registryMap.keySet()) {
			EntityMetaData entityMetaData = this.registryMap.get(entityName);
			// if this is a dependent entity continue
			if (entityMetaData.hasDependetEntity())
				continue;
			StringBuilder sqlBuilder = new StringBuilder();
			addCreateStatementHeader(sqlBuilder, entityName);
			for (String columnName : entityMetaData.columnNames()) {
				ColumnMetaData columnMetaData = entityMetaData.columns.get(columnName);
				sqlBuilder.append(", ");
				sqlBuilder.append(columnName);
				sqlBuilder.append(" " + columnMetaData.sqliteFieldType);
				// if length value is specified for Text attr
				if (columnMetaData.sqliteFieldType.equals(OrmConstants.SQLITE_TEXT) && columnMetaData.self.length() > 0)
					sqlBuilder.append("(" + columnMetaData.self.length() + ")");
			}
			sqlBuilder.append(", PRIMARY KEY(");
			sqlBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
			sqlBuilder.append(" " + entityMetaData.primaryKeyMetaData.orderby + ")");
			sqlBuilder.append(");");
			statements.add(sqlBuilder.toString());
			result.add(entityName);
		}
		return result;
	}
	
	/**
	 * creates Create Table statements for entities and add them to statements collection. if a create table statement already generated for entity skips it.
	 * 
	 * @param statements
	 * @param generatedEntities
	 */
	private final void generateCreateStatementsForDependents(final Collection<String> statements, final Set<String> generatedEntities) {
		// to avoid re-generate create statement for an entity again.
		
		for (String entityName : this.registryMap.keySet()) {
			EntityMetaData entityMetaData = this.registryMap.get(entityName);
			// non dependent entities already created in another generateCreateStatementsForNondependents
			if (!entityMetaData.hasDependetEntity() || generatedEntities.contains(entityName))
				continue;
			this.generateCreateStatement(entityName, statements, generatedEntities);
		}
	}
	
	/**
	 * recursive method , generates create statements for an entity and its dependent entities.
	 * 
	 * @param entityName
	 * @param statements
	 * @param generatedEntities
	 */
	private final void generateCreateStatement(final String entityName, final Collection<String> statements, final Set<String> generatedEntities) {
		EntityMetaData entityMetaData = this.registryMap.get(entityName);
		if (entityMetaData == null)
			throw new UnRegisteredEntityException(entityName);
		// first generate create statements for dependentEntities
		for (ForeignKeyMetaData foreignKeyMetaData : entityMetaData.dependentEntities.values()) {
			// if create statement already generated for dependent entity skip it
			if (generatedEntities.contains(foreignKeyMetaData.referenceEntityName))
				continue;
			// if create statement not generated , generate its statement before this one.
			generateCreateStatement(foreignKeyMetaData.referenceEntityName, statements, generatedEntities);
		}
		// after generating create statements for all dependents , we can continue for this entity
		StringBuilder sqlBuilder = new StringBuilder();
		addCreateStatementHeader(sqlBuilder, entityName);
		for (String columnName : entityMetaData.columnNames()) {
			ColumnMetaData columnMetaData = entityMetaData.columns.get(columnName);
			sqlBuilder.append(", ");
			sqlBuilder.append(columnName);
			sqlBuilder.append(" " + columnMetaData.sqliteFieldType);
			// if length value is specified for Text attr
			if (columnMetaData.sqliteFieldType.equals(OrmConstants.SQLITE_TEXT) && columnMetaData.self.length() > 0)
				sqlBuilder.append("(" + columnMetaData.self.length() + ")");
		}
		// Primary Key definition
		sqlBuilder.append(", PRIMARY KEY(");
		sqlBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
		sqlBuilder.append(" " + entityMetaData.primaryKeyMetaData.orderby + ")");
		// foreign key definitions
		for (String columnName : entityMetaData.dependentEntities.keySet()) {
			sqlBuilder.append(", FOREIGN KEY(");
			sqlBuilder.append(columnName);
			sqlBuilder.append(") REFERENCES ");
			ForeignKeyMetaData foreignKeyMetaData = entityMetaData.dependentEntities.get(columnName);
			sqlBuilder.append(foreignKeyMetaData.referenceEntityName);
			sqlBuilder.append("(" + foreignKeyMetaData.referenceColumnName + ")");
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
	private final void addCreateStatementHeader(final StringBuilder sqlBuilder, final String entityName) {
		sqlBuilder.append("CREATE TABLE ");
		sqlBuilder.append(entityName.toUpperCase());
		sqlBuilder.append("(");
		sqlBuilder.append(OrmConstants.PRIMARY_KEY_COLUMN_NAME);
		sqlBuilder.append(" INTEGER");
	}
	
	// ###########################################################################################################
	// END OF GENERATE STATEMENTS FOR DATABASE INITIALIZATION
	// ###########################################################################################################
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
		EntityMetaData entityMetaData = this.registryMap.get(key);
		if (entityMetaData == null)
			throw new UnRegisteredEntityException(entity.name());
		return entityMetaData.columnGetter(columnName).invoke(entity);
	}
	
	/**
	 * @param qualifiedName
	 * @param fieldName
	 */
	public final void setValueOf(final String entityName, final String columnName, final Object value) {
		
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
		String entityName = PersistenceUtil.getEntityName(obj.getClass());
		EntityMetaData metaData = this.registryMap.get(entityName);
		if (metaData == null)
			throw new UnRegisteredEntityException(entityName);
		Map<String, Object> result = new HashMap<String, Object>();
		for (String columnName : metaData.columns.keySet())
			result.put(columnName, metaData.columnGetter(columnName).invoke(obj));
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
		
		String entityName = PersistenceUtil.getEntityName(obj.getClass());
		
		EntityMetaData entityMetaData = this.registryMap.get(entityName);
		if (entityMetaData == null)
			throw new UnRegisteredEntityException(entityName);
		
		ContentValues values = new ContentValues();
		for (String columnName : entityMetaData.columns.keySet())
			this.addToContent(values, columnName, entityMetaData.columnGetter(columnName).invoke(obj), entityMetaData.columns.get(columnName));
		
		return values;
	}
	
	/**
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	private final void addToContent(final ContentValues values, final String columnName, final Object value, final ColumnMetaData columnMetaData) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
			if (columnMetaData.foreignKeyMetaData != null && !columnMetaData.foreignKeyMetaData.referenceFieldName.equals(OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE)) {
				this.addToContent(values, columnName, ReflectionUtil.invokeGetMethod(value, columnMetaData.foreignKeyMetaData.referenceFieldName, columnMetaData.foreignKeyMetaData.referenceFieldType), columnMetaData);
			}
			else
				values.put(columnName, ((Persistable) value).getId());
		}
		else
			throw new UnsupportedFieldTypeException(value.getClass().getName());
	}
	
	// ###########################################################################################################
	// META DATA DEFINITIONS
	// ###########################################################################################################
	/**
	 * keeps required data for an entity : *if it is dependent on other entities, *has indexes checks circular references. If there exist a circular reference between two entities @throws
	 * CircularForeignKeyException
	 * 
	 * @author Hamza Gumrah
	 */
	private final class EntityMetaData {
		
		/**
		 * if this entity has foreign keys, it should dependent on other entities. we have to create dependentEntities first during database creation since Sqlite does not support ALTER TABLE add
		 * constraint feature,
		 * 
		 * @see http://www.sqlite.org/omitted.html
		 */
		final Map<String, ForeignKeyMetaData> dependentEntities;
		
		final Map<String, ColumnMetaData> columns;
		
		PrimaryKeyMetaData primaryKeyMetaData;
		
		EntityMetaData() {
			this.dependentEntities = new HashMap<String, OrmRegistry.ForeignKeyMetaData>(0);
			this.columns = new HashMap<String, ColumnMetaData>();
		}
		
		final void put(final String columnName, final ColumnMetaData columnMetaData) {
			this.columns.put(columnName, columnMetaData);
			if (columnMetaData.foreignKeyMetaData != null) {
				this.dependentEntities.put(columnName, columnMetaData.foreignKeyMetaData);
			}
		}
		
		final Set<String> columnNames() {
			return this.columns.keySet();
		}
		
		final Method columnGetter(String columnName) {
			return this.columns.get(columnName).getMethod;
		}
		
		final Method columnSetter(String columnName) {
			return this.columns.get(columnName).setMethod;
		}
		
		final boolean hasDependetEntity() {
			return this.dependentEntities.size() > 0;
		}
	}
	
	/**
	 * keeps required information for a column e.g if it is primary key, has foreign key constraint related fields set and get methods e.t.c
	 * 
	 * @author Hamza Gumrah
	 */
	private final class ColumnMetaData {
		
		private final Column self;
		
		private final String sqliteFieldType;
		
		private final Method getMethod;
		
		private final Method setMethod;
		
		private final ForeignKeyMetaData foreignKeyMetaData;
		
		private final boolean isDate;
		
		ColumnMetaData(Column self, Method getMethod, Method setMethod, ForeignKeyMetaData foreignKeyMetaData, Class<?> fieldType) {
			super();
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
			return self.nullable();
		}
		
		private final boolean isForeignKey() {
			return this.foreignKeyMetaData != null;
		}
	}
	
	/**
	 * keeps meta data for foreign keys, reference field name reference field type
	 * 
	 * @author Hamza Gumrah
	 */
	private final class ForeignKeyMetaData {
		
		private final String referenceEntityName;
		
		private final String referenceColumnName;
		
		private final String referenceFieldName;
		
		private final Class<?> referenceFieldType;
		
		ForeignKeyMetaData(String referenceEntityName) {
			this(referenceEntityName, OrmConstants.PRIMARY_KEY_COLUMN_NAME, OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE, Long.class);
		}
		
		ForeignKeyMetaData(String referenceEntityName, String referenceColumnName, String referenceFieldName, Class<?> referenceFieldType) {
			super();
			this.referenceEntityName = referenceEntityName;
			if (referenceFieldName != null && referenceFieldType != null) {
				this.referenceFieldName = referenceFieldName;
				this.referenceFieldType = referenceFieldType;
				this.referenceColumnName = referenceColumnName;
			}
			else {
				this.referenceFieldName = OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE;
				this.referenceFieldType = Long.class;
				this.referenceColumnName = OrmConstants.PRIMARY_KEY_COLUMN_NAME;
			}
		}
		
	}
	
	/**
	 * keeps meta data for primary keys OrderType e.f DESC
	 * 
	 * @author Hamza Gumrah
	 */
	private final class PrimaryKeyMetaData {
		
		private final OrderType orderby;
		
		private final Method getMethod;
		
		private final Method setMethod;
		
		PrimaryKeyMetaData(OrderType orderby, Method getMethod, Method setMethod) {
			this.orderby = orderby;
			this.getMethod = getMethod;
			this.setMethod = setMethod;
		}
	}
	// ###########################################################################################################
	// END OF META DATA DEFINITIONS
	// ###########################################################################################################
	
}
