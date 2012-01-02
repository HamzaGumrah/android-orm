package com.android.orm;

/**
 * contains constant values for android-orm project
 * 
 * @author Hamza Gumrah
 */
public interface OrmConstants {
	
	public static final String PRIMARY_KEY_FIELD_NAME = "id";
	
	public static final String PRIMARY_KEY_COLUMN_NAME = "_id";
	
	public static final String DEFAULT_FOREIGN_KEY_REFERENCE = "id";
	
	public static final String SQLITE_TEXT = "TEXT";
	
	public static final String SQLITE_NUMBER = "INTEGER";
	
	public static final String SQLITE_BLOB = "BLOB";
	
	public static final String SQLITE_DATE = "INTEGER";
	
	public static final String SQLITE_REAL = "REAL";
	/**
	 * if entity is not persisted getId() should return 0
	 */
	public static final long NOT_PERSISTED_ID = 0;
}
