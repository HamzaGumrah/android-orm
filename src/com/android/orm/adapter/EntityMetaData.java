package com.android.orm.adapter;

import java.util.Set;

/**
 * keeps required data for an entity : *if it is dependent on other entities, *has indexes checks circular references. If there exist a circular reference between two entities @throws
 * CircularForeignKeyException an entity can map to multiple tables when Inheritence is JOINED
 * 
 * @author Hamza Gumrah
 */
final class EntityMetaData {
	
	
	private final Set<TableMetaData> mappedTables;
	
	private final Class<?> clazz;
	/**
	 * if this entity has foreign keys, it should dependent on other entities. we have to create dependentEntities first during database creation since Sqlite does not support ALTER TABLE add
	 * constraint feature,
	 * 
	 * @see http://www.sqlite.org/omitted.html
	 * 
	 * if entity's mapped table or tables have foreign keys,cross tables than entity is dependent.
	 */
	private final boolean isDependent;
	//table meta data does not cover below informations
//	final Set<RelationalMetaData.ManyToManyMetaData> manyToMany;
	/**
	 * tables do not have oneToMany information we need to keep them in entity
	 * during persistence oneToMany entities should be persist as well.
	 * and during get, oneToMany entities should be fetched as well.
	 */
	private final Set<OneToManyMetaData> oneToMany;
//	/**
//	 * tables do not have oneToOne information we need to keep them in entity
//	 * during persistence oneToMany entities should be persist as well.
//	 * and during get, oneToMany entities should be fetched as well.
//	 */
//	private final Set<RelationalMetaData.OneToOneMetaData> oneToOne;
	
	public EntityMetaData(Set<TableMetaData> mappedTables, Class<?> clazz,Set<OneToManyMetaData> oneToManies) {
		this.mappedTables = mappedTables;
		this.clazz = clazz;
		this.oneToMany = oneToManies;
		boolean dependent = false;
		for (TableMetaData metaData : mappedTables) {
			if (metaData.hasForeignKey()) {
				dependent = true;
				break;
			}
			else if(metaData.hasCrossTable()){
				dependent = true;
				break;
			}
		}
		this.isDependent = dependent;
	}
	
	/**
	 * @return true if this entity depends on another entity
	 */
	final boolean isDependent() {
		return this.isDependent;
	}

	/**
	 * 
	 * @return if Inheritence is SingleTable will return one table , which is the reflection of 
	 * entity in database,
	 * else if Joined table will return a table set, which are chained to each other on parent-child
	 * relation.
	 */
	public Set<TableMetaData> getMappedTables() {
		return mappedTables;
	}

	/**
	 * 
	 * @return class of the entity
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * tables do not have oneToMany information we need to keep them in entity
	 * during persistence oneToMany entities should be persist as well.
	 * and during get, oneToMany entities should be fetched as well.
	 * @return OneToMany entities
	 */
	public Set<OneToManyMetaData> getOneToMany() {
		return oneToMany;
	}
	/**
	 * 
	 * @return number of tables which this entity reflected
	 */
	public int getTableCount(){
		return this.mappedTables.size();
	}

}
