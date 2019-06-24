package com.yoloho.enhanced.data.sharding.strategy;

public interface ShardingHandler {
	
	public String getShardedTable(ShardingContext context);

}
