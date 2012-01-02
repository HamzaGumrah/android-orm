package com.android.orm.adapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class TableMetaData {
	
	private String name = null;
	
	// key:columnName
	private final Map<String, ForeignKeyMetaData> foreignKeys;
	
	private final Set<ColumnMetaData> columns;
	
	// private final PrimaryKeyMetaData primaryKeyMetaData;
	
	// if InheritenceStrategy.JOINED and this table has depending to a main table.
	
	private final Set<String> crossTables;
	
	private String parentTableName = null;
	
	private final Set<String> children;
	
	public TableMetaData() {
		this.foreignKeys = new HashMap<String, ForeignKeyMetaData>();
		this.columns = new HashSet<ColumnMetaData>();
		this.children = new HashSet<String>(0);
		this.crossTables = new HashSet<String>(0);
	}
	
	final boolean hasForeignKey() {
		return this.foreignKeys != null;
	}
	
	final boolean isChild() {
		return this.parentTableName != null;
	}
	
	final boolean hasCrossTable() {
		return this.crossTables.size() == 0;
	}
	
	/**
	 * initiates parent. After first initiation it is not possible to change parent.
	 * 
	 * @param parent
	 */
	final void setParent(String parentTableName) {
		if (this.parentTableName == null) {
			synchronized (this.parentTableName) {
				if (this.parentTableName == null)
					this.parentTableName = parentTableName;
			}
		}
		// TODO already has parent
	}
	
	final String getParent() {
		return this.parentTableName;
	}
	
	final void addCrossTable(String crossTableName) {
		this.crossTables.add(crossTableName);
	}
	
	/**
	 * if childTable has no name or name wtih "" than return false
	 * 
	 * @param childTable
	 * @return true if added successfully
	 */
	final boolean addChild(TableMetaData childTable) {
		if (childTable.name == null || childTable.name == "")
			return false;
		this.children.add(childTable.name);
		childTable.setParent(this.name);
		return true;
	}
	
	public final void addColumn(ColumnMetaData columnMetaData) {
		this.columns.add(columnMetaData);
		if (columnMetaData.isForeignKey())
			this.foreignKeys.put(columnMetaData.getColumnName(), columnMetaData.getForeignKeyMetaData());
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, ForeignKeyMetaData> getForeignKeys() {
		return foreignKeys;
	}
	
	public Set<ColumnMetaData> getColumns() {
		return columns;
	}
	
	public Set<String> getCrossTableNames() {
		return crossTables;
	}
	
	public Set<String> getChildren() {
		return children;
	}
	
	public void setName(String name) {
		if (this.name == null) {
			synchronized (this.name) {
				if (this.name == null)
					this.name = name;
			}
		}
	}
	
	/**
	 * this method is required to sum up required metaData of the Table merge table and current table name should be the same.
	 * 
	 * @param metaData
	 */
	public void merge(TableMetaData metaData) {
		// can not merge different tables
		if (this.name != metaData.name)
			return;
		// if childData already exist
		for (String child : metaData.children)
			this.children.add(child);
		// cross tables
		for (String crossTableName : metaData.crossTables)
			this.crossTables.add(crossTableName);
		
	}
	
}
