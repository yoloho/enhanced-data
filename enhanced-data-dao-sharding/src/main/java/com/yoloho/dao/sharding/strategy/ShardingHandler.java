package com.yoloho.dao.sharding.strategy;

public interface ShardingHandler {
	
	public String getShardedTable(ShardingContext context);

}
