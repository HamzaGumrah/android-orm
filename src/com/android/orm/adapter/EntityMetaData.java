package com.android.orm.adapter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * keeps required data for an entity : *if it is dependent on other entities, *has indexes checks circular references. If there exist a circular reference between two entities @throws
 * CircularForeignKeyException
 * 
 * @author Hamza Gumrah
 */
final class EntityMetaData {
	
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
		this.dependentEntities = new HashMap<String, ForeignKeyMetaData>(0);
		this.columns = new HashMap<String, ColumnMetaData>();
	}
	
	final void put(final String columnName, final ColumnMetaData columnMetaData) {
		this.columns.put(columnName, columnMetaData);
		if (columnMetaData.getForeignKeyMetaData() != null) {
			this.dependentEntities.put(columnName, columnMetaData.getForeignKeyMetaData());
		}
	}
	
	final Set<String> columnNames() {
		return this.columns.keySet();
	}
	
	final Method columnGetter(String columnName) {
		return this.columns.get(columnName).getGetter();
	}
	
	final Method columnSetter(String columnName) {
		return this.columns.get(columnName).getSetter();
	}
	
	final boolean hasDependetEntity() {
		return this.dependentEntities.size() > 0;
	}
	
	public Map<String, ForeignKeyMetaData> getDependentEntities() {
		return dependentEntities;
	}
	
	public Map<String, ColumnMetaData> getColumns() {
		return columns;
	}
	
	public PrimaryKeyMetaData getPrimaryKeyMetaData() {
		return primaryKeyMetaData;
	}
	
}
