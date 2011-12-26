package com.android.orm.annotation;

/**
 * abstract classes which have column annotations on their fields , should have this entity current version does not allow creation single table inheritence tables, each entity which extend from
 * another will be mapped to different table.
 * 
 * @author Hamza Gumrah
 */
public @interface AbstractEntity {
	
}
