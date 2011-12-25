package com.android.orm.adapter;

import java.util.Collection;
import java.util.Map;

import com.android.orm.Persistable;

/**
 * @author Hamza Gumrah
 */
public interface DatabaseAdapter {
	
	/**
	 * gets writableDatabases;
	 */
	void open();
	
	/**
	 * closes database connection
	 */
	void close();
	
	/**
	 * enables/disables foreign key support for database. if you will drop all tables , disabling all foreign keys will be helpful
	 * 
	 * @param enabled
	 */
	void setForeignKeySupport(boolean enabled);
	
	/**
	 * drops all tables in a single transaction
	 */
	void dropAll();
	
	/**
	 * drops specified entities from database in a single transaction. if there are foreignkeys it will throw an exception.
	 * 
	 * @param entityNames @Entity.name() list
	 */
	void drop(String... entityNames);
	
	/**
	 * inserts an entity to database
	 * 
	 * @param entity
	 */
	<T extends Persistable> void persist(T entity);
	
	/**
	 * @param systemId
	 * @param clazz
	 * @return entity with corresponding id else null
	 */
	<T extends Persistable> T get(long id, Class<T> clazz);
	
	/**
	 * enables to write any custom query
	 * 
	 * @param tableName
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	Collection<Map<String, Object>> query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);
	
	/**
	 * updates entity
	 * 
	 * @param entity
	 */
	void update(Persistable entity);
	
	/**
	 * deletes the entity from database.
	 * 
	 * @param entity
	 */
	void delete(Persistable entity);
	
}
