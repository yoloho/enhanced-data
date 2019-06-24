package com.yoloho.enhanced.data.sharding.strategy.handler;

import com.yoloho.enhanced.data.sharding.api.ShardingFactor;
import com.yoloho.enhanced.data.sharding.strategy.ShardingContext;
import com.yoloho.enhanced.data.sharding.strategy.ShardingHandler;

public class HashedShardingHandler implements ShardingHandler{
	

	@Override
	public String getShardedTable(ShardingContext context) {
		if(context.getShardedFactors().isEmpty()) {
			throw new RuntimeException("解析分表名称出错:shardedFactor=null");
		}
		ShardingFactor shardedFactor = context.getShardedFactors().get(0);
		if(shardedFactor.getFactorValue()==null) {
			throw new RuntimeException("解析分表名称出错:shardedFactor数据为null");
		}
		int shard = this.shardingByHash(shardedFactor.getFactorValue(), 8);
		return context.getTableModel() + "_" + shard;
	}
	
	private int shardingByHash(Object value, int fixed) {
		if(fixed == 0) {
			return 0;
		}
		return Math.abs(value.hashCode()%fixed);
	}

}