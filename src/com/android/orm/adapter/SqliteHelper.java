package com.android.orm.adapter;

import java.sql.Date;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;

abstract class SqliteHelper {
	
	/**
	 * Integer,Long,Short,Byte,Boolean,Date are recorded as INTEGER String and Enum types are recorded as TEXT Double and Float types are recorded as REAL byte[] is BLOB
	 * 
	 * @return sqlite column type's name in uppercase
	 */
	static String getSqliteTypeName(Class<?> clazz) {
		
		if (Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)
				|| Boolean.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_NUMBER;
		if (String.class.isAssignableFrom(clazz) || Enum.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_TEXT;
		if (Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_REAL;
		if (byte[].class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_BLOB;
		;
		if (Persistable.class.isAssignableFrom(clazz))
			return OrmConstants.SQLITE_NUMBER;
		return "";
	}
}
