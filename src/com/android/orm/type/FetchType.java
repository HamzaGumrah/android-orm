package com.android.orm.type;

/**
 * EAGER : will get reference object in any get method of @see DatabaseAdapter LAZY : will return null for reference object in standard get() of @see DatabaseAdapter, those types can be fetched using
 * getByLazy
 * 
 * @author Hamza
 */
public enum FetchType {
	EAGER, LAZY;
}
