package com.yoloho.enhanced.data.sharding.strategy;

import java.util.HashMap;
import java.util.Map;

public class StrategyUtils {
	
	private static Map<String, ShardingStrategy> strategyMap = new HashMap<>();
	
	public static ShardingStrategy get(String strategy) {
		if(strategyMap.isEmpty()) {
			synchronized(strategyMap) {
				String clazz = ShardingStrategy.class.getName();
				for(ShardingStrategy curStrategy : ShardingStrategy.values()) {
					strategyMap.put(clazz+"."+curStrategy.name(), curStrategy);
				}
			}
		}
		ShardingStrategy shardedStrategy = strategyMap.get(strategy);
		if(shardedStrategy!=null) {
			return shardedStrategy;
		}else {
			throw new RuntimeException("无效的分表策略：ShardedStrategy="+strategy);
		}
	};

}
