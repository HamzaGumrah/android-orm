package com.android.orm.adapter;

import java.lang.reflect.Method;

import com.android.orm.annotation.Column;
import com.android.orm.util.PersistenceUtil;

/**
 * keeps required information for a column e.g if it is primary key, has foreign key constraint related fields set and get methods e.t.c
 * 
 * @author Hamza Gumrah
 */
final class ColumnMetaData {
	
	private final Column self;
	
	private final String sqliteFieldType;
	
	private final Method getMethod;
	
	private final Method setMethod;
	
	private final ForeignKeyMetaData foreignKeyMetaData;
	
	private final boolean isDate;
	
	private final String name;
	
	ColumnMetaData(Column self, Method getMethod, Method setMethod, ForeignKeyMetaData foreignKeyMetaData, Class<?> fieldType) {
		this(self,getMethod,setMethod,foreignKeyMetaData,fieldType,self.name());
	}
	ColumnMetaData(Column self, Method getMethod, Method setMethod, ForeignKeyMetaData foreignKeyMetaData, Class<?> fieldType,String name) {
		super();
		if(name.equals(""))
			throw new InstantiationError("Column name must defined");
		this.isDate = PersistenceUtil.isDate(fieldType);
		this.self = self;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
		this.foreignKeyMetaData = foreignKeyMetaData;
		// if this is a foreign key , sqliteFieldType should be foreign key reference column type
		if (this.foreignKeyMetaData != null)
			this.sqliteFieldType = SqliteHelper.getSqliteTypeName(this.foreignKeyMetaData.getReferenceFieldType());
		else
			this.sqliteFieldType = SqliteHelper.getSqliteTypeName(fieldType);
		this.name = name;
	}
	final boolean isNullable() {
		return self.nullable();
	}
	
	final boolean isForeignKey() {
		return this.foreignKeyMetaData != null;
	}
	
	final String getSqliteFieldType() {
		return this.sqliteFieldType;
	}
	
	final Method getGetter() {
		return this.getMethod;
	}
	
	final Method getSetter() {
		return this.setMethod;
	}
	
	final boolean isDate() {
		return this.isDate;
	}
	
	final ForeignKeyMetaData getForeignKeyMetaData() {
		return this.foreignKeyMetaData;
	}
	
	final Class<?> getFieldType() {
		return this.getMethod.getReturnType();
	}
	
	final int length() {
		return self.length();
	}
	
	final String getColumnName() {
		return this.name;
	}
}
