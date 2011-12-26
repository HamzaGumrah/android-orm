package com.android.orm.adapter;

import com.android.orm.OrmConstants;

/**
 * keeps meta data for foreign keys, reference field name reference field type
 * 
 * @author Hamza Gumrah
 */
final class ForeignKeyMetaData {
	
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
	
	public String getReferenceEntityName() {
		return referenceEntityName;
	}
	
	public String getReferenceColumnName() {
		return referenceColumnName;
	}
	
	public String getReferenceFieldName() {
		return referenceFieldName;
	}
	
	public Class<?> getReferenceFieldType() {
		return referenceFieldType;
	}
	
}
