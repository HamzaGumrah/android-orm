package com.android.orm.adapter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
	 * inserts an entity to database
	 * 
	 * @param entity
	 * @throws PersistenceException
	 */
	<T extends Persistable> void persist(T entity);
	
	/**
	 * inserts entities to database in a single transaction
	 * roll backs if any exception occurs
	 * @param <T>
	 * @param entityCollection if entityCollection is null or has no records than returns. 
	 * @throws PersistenceException
	 */
	<T extends Persistable> void persist(Collection<T> entityCollection);
	
	/**
	 * inserts entities to database in a single transaction
	 * roll backs if any exception occurs
	 * @param <T>
	 * @param entitySet if entitySet is null or has no records than returns.
	 * @throws PersistenceException
	 */
	<T extends Persistable> void persist(Set<T> entitySet);
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
