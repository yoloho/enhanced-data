package com.yoloho.enhanced.data.sharding.strategy;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.yoloho.enhanced.data.sharding.api.ShardingFactor;
import com.yoloho.enhanced.data.sharding.api.ShardingInfo;

public class ShardingContext implements Serializable{
	
	private static final long serialVersionUID = 4590747204428923679L;

	private ShardingInfo shardedInfo;
	private List<ShardingFactor> shardedFactors = new ArrayList<>();
	
	/**
	 * 实例化
	 * @param shardedInfo
	 * @param entity
	 * @return
	 * @throws Exception 
	 */
	public static ShardingContext instance(ShardingInfo shardedInfo, Object entity) throws Exception {
		ShardingContext context = new ShardingContext();
		context.setShardedInfo(shardedInfo);
		
		for(Field curField : shardedInfo.getShardedFields()) {
			curField.setAccessible(true);
			context.addShardedFactor(curField.getName(), curField.get(entity));
		}
		
		return context;
	}

	/**
	 * 实例化
	 * @param shardedInfo		分片描述信息
	 * @param shardedFactor		分片因子
	 * @return
	 */
	public static ShardingContext instance(ShardingInfo shardedInfo, ShardingFactor shardedFactor) {
		ShardingContext context = new ShardingContext();
		context.setShardedInfo(shardedInfo);
		context.addShardedFactor(shardedFactor);
		return context;
	}
	
	/**
	 * 实例化
	 * @param shardedInfo		分片描述信息
	 * @param shardedFactor		分片因子
	 * @return
	 */
	public static ShardingContext instance(ShardingInfo shardedInfo, List<ShardingFactor> shardedFactors) {
		ShardingContext context = new ShardingContext();
		context.setShardedInfo(shardedInfo);
		context.setShardedFactors(shardedFactors);
		return context;
	}

	public ShardingInfo getShardedInfo() {
		return shardedInfo;
	}
	protected void setShardedInfo(ShardingInfo shardedInfo) {
		this.shardedInfo = shardedInfo;
	}

	public List<ShardingFactor> getShardedFactors() {
		return shardedFactors;
	}
	public void setShardedFactors(List<ShardingFactor> listFactors) {
		this.shardedFactors = listFactors;
	}
	public void addShardedFactor(ShardingFactor shardedFactor) {
		shardedFactors.add(shardedFactor);
	}
	public void addShardedFactor(String factorParam, Object factorValue) {
		shardedFactors.add(new ShardingFactor(factorParam, factorValue));
	}

	public String getTableModel() {
		return shardedInfo.getTable();
	}
	
	public ShardingStrategy getShardedStrategy() {
		return shardedInfo.getStrategy();
	}
	
	public String getShardedHandler() {
		return shardedInfo.getHandler();
	}

}