package com.yoloho.dao.sharding.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.yoloho.dao.sharding.annotation.ShardedFactor;
import com.yoloho.dao.sharding.strategy.ShardingStrategy;

public class ShardingInfo implements java.io.Serializable{

	private static final long serialVersionUID = 7418067451627282465L;

	private String dao;
	private String table;
	private ShardingStrategy strategy;
	private String handler;
	private Class<?> entityClazz;
	private int mode;
	private List<Field> shardedFields = new ArrayList<>();
	
	public ShardingInfo(Class<?> entityClazz) {
		this.setEntityClazz(entityClazz);
	}
	
	public String getDao() {
		return dao;
	}
	public void setDao(String dao) {
		this.dao = dao;
	}
	
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	
	public ShardingStrategy getStrategy() {
		return strategy;
	}
	public void setStrategy(ShardingStrategy strategy) {
		this.strategy = strategy;
	}
	
	public String getHandler() {
		return handler;
	}
	public void setHandler(String handler) {
		this.handler = handler;
	}
	
	public Class<?> getEntityClazz() {
		return entityClazz;
	}
	public void setEntityClazz(Class<?> entityClazz) {
		this.entityClazz = entityClazz;
		this.parseShardedFields();
	}

	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}

	public List<Field> getShardedFields() {
		return shardedFields;
	}

	private void parseShardedFields() {
		for(Field field : entityClazz.getDeclaredFields()) {
			if(field.isAnnotationPresent(ShardedFactor.class)) {
				shardedFields.add(field);
			}
		}
	}
	
}