package com.android.orm.adapter;

import android.util.Pair;

final class CrossTableMetaData {
	
	// {tableName,Entity.class}
	private final String name;
	
	private final Pair<String, Class<?>> firstTableNameEntityPair;
	
	private final Pair<String, Class<?>> secondTableNameEntityPair;
	/**
	 * cross table name will be firstTableNameEntityPair.first+"_"+secondTableNameEntityPair.first+"_CROSS";
	 * @param firstTableNameEntityPair
	 * @param secondTableNameEntityPair
	 */
	public CrossTableMetaData(Pair<String, Class<?>> firstTableNameEntityPair, Pair<String, Class<?>> secondTableNameEntityPair) {
		this.firstTableNameEntityPair = firstTableNameEntityPair;
		this.secondTableNameEntityPair = secondTableNameEntityPair;
		this.name = firstTableNameEntityPair.first+"_"+secondTableNameEntityPair.first+"_CROSS";
	}
	
	public Pair<String, Class<?>> getFirstTableNameEntityPair() {
		return firstTableNameEntityPair;
	}
	
	public Pair<String, Class<?>> getSecondTableNameEntityPair() {
		return secondTableNameEntityPair;
	}

	
	public String getName() {
		return name;
	}
	
}
