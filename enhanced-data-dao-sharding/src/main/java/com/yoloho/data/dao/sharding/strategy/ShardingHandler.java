package com.yoloho.data.dao.sharding.strategy;

public interface ShardingHandler {
	
	public String getShardedTable(ShardingContext context);

}
