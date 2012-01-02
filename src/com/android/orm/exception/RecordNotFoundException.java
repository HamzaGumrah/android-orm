package com.android.orm.exception;

import com.android.orm.OrmConstants;
import com.android.orm.Persistable;
import com.android.orm.util.PersistenceUtil;


public class RecordNotFoundException extends PersistenceException {
	
	private static final long serialVersionUID = 1L;
	
	public RecordNotFoundException(Persistable persistable) {
		super(persistable,"");
	}
	@Override
	public String getMessage() {
		return "No Record with "+OrmConstants.PRIMARY_KEY_COLUMN_NAME + " = " + this.persistable.getId() +"for entity : "+ PersistenceUtil.getEntityName(persistable.getClass());
	}
	
}
