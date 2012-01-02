package com.android.orm;

import java.io.Serializable;

/**
 * All persistable objects should have an id field with @PrimaryKey annotation.
 * This field will be mapped as primaryKey to database. 
 *  
 * @author Hamza Gumrah
 */
public interface Persistable extends Serializable {
	
	public Long getId();
	
	public Long setId(Long id);
}
