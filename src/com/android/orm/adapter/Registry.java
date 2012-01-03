package com.android.orm.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.android.orm.OrmConstants;
import com.android.orm.annotation.Column;
import com.android.orm.annotation.Entity;
import com.android.orm.annotation.ForeignKey;
import com.android.orm.annotation.ManyToMany;
import com.android.orm.annotation.OneToMany;
import com.android.orm.annotation.Table;
import com.android.orm.exception.ManyToManyViolation;
import com.android.orm.exception.MultiplePrimaryKeyException;
import com.android.orm.exception.PrimaryKeyNotFoundException;
import com.android.orm.exception.UnsupportedForeignKeyReferenceException;
import com.android.orm.exception.UnsupportedPrimaryKeyException;
import com.android.orm.util.PersistenceUtil;
import com.android.orm.util.ReflectionUtil;

import android.util.Log;

/**
 * keeps MetaData information regarding entities,tables,columns,foreignkeys,primarykeys e.t.c. it is generated during adapter creation, should be used only as SingleTone. DatabaseAdapterFactory
 * handles singleTone access to registry.
 * 
 * @author Hamza Gumrah
 */
final class Registry {
	
	private final String TAG = "Registry";
	
	// keeps entity.getClass().getName(),EntityMetaData
	private Map<String, EntityMetaData> entitis;
	
	// to reach tableMetaData faster
	// keep them in parent-child order
	private Map<String, TableMetaData> tables;
	
	private Map<String, CrossTableMetaData> crossTables;
	
	/**
	 * @param entityQualifiedNames qualifiedNames of entity classes, should be Set to avoid duplicate elements.
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public Registry(final Set<String> entityQualifiedNames) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		this.entitis = new HashMap<String, EntityMetaData>();
		for (String qualifiedName : entityQualifiedNames) {
			EntityMetaData entityMetaData = generateMetaData(qualifiedName);
			this.entitis.put(qualifiedName, entityMetaData);
		}
	}
	
	/**
	 * @return generated EntityMetaData for Class.forName(qualifiedName)
	 * @throws ClassNotFoundException if qualifiedName does not a Class's name.
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private final EntityMetaData generateMetaData(final String qualifiedName) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		Class<?> clazz = Class.forName(qualifiedName);
		// skip the class if it can not be mapped to table.
		if (SqliteHelper.mappable(clazz)) {
			Log.w(TAG, "class with qualifiedName : " + qualifiedName
					+ " can not be mapped to table since it is missing @Entity annotation or Persistable interface ");
			return null;
		}
		Class<?> type = clazz;
		// EntityMetaData entityMetaData = null;
		// to reach declared fields of super classes, it is required to loop until Object.class
		boolean hasPrimaryKey = false;
		// tables related to entity
		Set<TableMetaData> tableMetaDatas = new HashSet<TableMetaData>();
		TableMetaData metaData = new TableMetaData();
		String lastTableName = "";
		Set<OneToManyMetaData> oneToManyDatas = new HashSet<OneToManyMetaData>();
		while (Object.class.equals(type)) {
			Field[] fields = type.getDeclaredFields();
			Class<?> fieldType = null;
			for (Field field : fields) {
				fieldType = field.getType();
				if (field.getName().equals(OrmConstants.PRIMARY_KEY_FIELD_NAME)) {
					// if id field is not a Long.class than throw exception
					if (!Long.class.isAssignableFrom(fieldType))
						throw new UnsupportedPrimaryKeyException(qualifiedName, fieldType.getName());
					// if id field is both defined in superclass and its child
					if (hasPrimaryKey)
						throw new MultiplePrimaryKeyException(qualifiedName);
					
				}
				
				else if (field.isAnnotationPresent(ManyToMany.class)) {
					if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Column.class)
							|| field.isAnnotationPresent(ForeignKey.class))
						Log.w(TAG, "OneToMany,Column,ForeignKey annotations over ManyToMany will be underestimated");
					ManyToMany self = field.getAnnotation(ManyToMany.class);
					if (self.targetEntityClass() == null) {
						Log.e(TAG, "targetEntityClass of ManyToMany relationship can not be null");
						throw new ManyToManyViolation(field.getName(), type);
					}
					else if (SqliteHelper.mappable(self.targetEntityClass())) {
						Log.e(TAG, "TargetEntityClass " + self.targetEntityClass()
								+ " can not be mapped to database; skipping OneToMany RelationShip for type : "
								+ type.getSimpleName() + " field : " + field.getName());
						throw new ManyToManyViolation(field.getName(), type);
					}
					// TODO how to find table Names?
				}
				
				else if (field.isAnnotationPresent(OneToMany.class)) {
					if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(ForeignKey.class))
						Log.w(TAG, "Column and ForeignKey annotations over OneToMany will be underestimated");
					OneToMany self = field.getAnnotation(OneToMany.class);
					if (self.targetEntityClass() == null)
						Log.w(TAG, "TargetEntityClass can not be null; skipping OneToMany RelationShip for type : "
								+ type.getSimpleName() + " field : " + field.getName());
					else if (!SqliteHelper.mappable(self.targetEntityClass()))
						Log.w(TAG, "TargetEntityClass can not be mapped to database; skipping OneToMany RelationShip for type : "
								+ type.getSimpleName() + " field : " + field.getName());
					
					if (fieldType.isAssignableFrom(Map.class)) {
						// TODO check if mapKey exist
					}
					
					Method getMethod = ReflectionUtil.findGetMethod(clazz, field.getName());
					Method setMethod = ReflectionUtil.findSetMethod(clazz, fieldType, field.getName());
					
					oneToManyDatas.add(new OneToManyMetaData(self, getMethod, setMethod));
					
				}
				else if (field.isAnnotationPresent(Column.class)) {
					PersistenceUtil.isFieldTypeSupported(fieldType);
					Column column = field.getAnnotation(Column.class);
					String columnName = column.name();
					if (columnName.equals("")) {
						Log.w(TAG, "Columns strongly couraged to have name(), else fieldName will be mapped as column name ");
						columnName = field.getName();
					}
					ForeignKeyMetaData foreignKeyMetaData = null;
					if (field.isAnnotationPresent(ForeignKey.class)) {
						if (SqliteHelper.mappable(fieldType)) {
							String foreignKeyReference = field.getAnnotation(ForeignKey.class).reference();
							if (!foreignKeyReference.equals(OrmConstants.DEFAULT_FOREIGN_KEY_REFERENCE)) {
								Class<?> referenceType = ReflectionUtil.findFieldType(fieldType, foreignKeyReference);
								if (!PersistenceUtil.isForeignKeyReferenceSupported(referenceType))
									throw new UnsupportedForeignKeyReferenceException(qualifiedName, field.getName(), foreignKeyReference, referenceType.getName());
								foreignKeyMetaData = new ForeignKeyMetaData(fieldType.getName(), columnName, foreignKeyReference, referenceType);
							}
							else
								foreignKeyMetaData = new ForeignKeyMetaData(fieldType.getName());
						}
						else
							Log.w(TAG, qualifiedName + " has a foreignKey annotation on field : " + field.getName()
									+ " which can not be mapped to database table");
						
					}
					Method getMethod = ReflectionUtil.findGetMethod(clazz, field.getName());
					Method setMethod = ReflectionUtil.findSetMethod(clazz, fieldType, field.getName());
					ColumnMetaData columnMetaData;
					if (column.name().equals(""))
						columnMetaData = new ColumnMetaData(column, getMethod, setMethod, foreignKeyMetaData, fieldType, field.getName());
					else
						columnMetaData = new ColumnMetaData(column, getMethod, setMethod, foreignKeyMetaData, fieldType);
					metaData.addColumn(columnMetaData);
				}
				
			}
			// set table name add it to registry
			if (type.isAnnotationPresent(Table.class)) {
				
				Table table = type.getAnnotation(Table.class);
				String tableName = null;
				if (table.name().equals(""))
					tableName = type.getSimpleName();
				else
					tableName = table.name();
				lastTableName = tableName;
				metaData.setName(tableName);
				TableMetaData tMetaData = this.tables.get(metaData.getName());
				if (tMetaData != null) {
					tMetaData.merge(metaData);
					tableMetaDatas.add(tMetaData);
				}
				else {
					this.tables.put(metaData.getName(), metaData);
					tableMetaDatas.add(metaData);
				}
				
				metaData = new TableMetaData();
			}
			type = type.getSuperclass();
			// end checking super classes
			if (!type.isAnnotationPresent(Entity.class)) {
				// if there exist no primary key
				if (!hasPrimaryKey)
					throw new PrimaryKeyNotFoundException(qualifiedName);
				if (lastTableName.equals("")) {
					// TODO throw table exception
				}
				metaData.setName(lastTableName);
				this.tables.get(lastTableName).merge(metaData);
			}
		}
		return new EntityMetaData(tableMetaDatas, clazz, oneToManyDatas);
	}
	
	public Map<String, EntityMetaData> getEntityRegistry() {
		return this.entitis;
	}
	
	public EntityMetaData getEntityMetaData(String entityName) {
		return this.entitis.get(entityName);
	}
	
	public Map<String,TableMetaData> getTableRegistry() {
		return this.tables;
	}
	
	public TableMetaData getTableMetaData(String tableName) {
		return this.tables.get(tableName);
	}
	
}
