package com.android.orm;

import java.io.Serializable;

/**
 * All persistable objects should have an id field. It is suggested that this field should be PrimaryKey
 * 
 * @author Hamza Gumrah
 */
public interface Persistable extends Serializable {
	
	public Long getId();
	
	public Long setId(Long id);
}
