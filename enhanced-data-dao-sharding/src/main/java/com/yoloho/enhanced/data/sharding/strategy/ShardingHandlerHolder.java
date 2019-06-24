package com.yoloho.enhanced.data.sharding.strategy;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoloho.enhanced.data.sharding.strategy.handler.DayShardingHandler;
import com.yoloho.enhanced.data.sharding.strategy.handler.HashedShardingHandler;
import com.yoloho.enhanced.data.sharding.strategy.handler.MonthShardingHandler;
import com.yoloho.enhanced.data.sharding.strategy.handler.YearShardingHandler;

public class ShardingHandlerHolder {
	
	public static final Logger logger = LoggerFactory.getLogger(ShardingHandlerHolder.class);

	private static Map<ShardingStrategy, ShardingHandler> defaultShardingHandlers = new HashMap<>();
	private static Map<String, ShardingHandler> shardingHandlers = new HashMap<>();
	
	static {
		defaultShardingHandlers.put(ShardingStrategy.TIME_YEAR, new YearShardingHandler());
		defaultShardingHandlers.put(ShardingStrategy.TIME_MONTH, new MonthShardingHandler());
		defaultShardingHandlers.put(ShardingStrategy.TIME_DAY, new DayShardingHandler());
		defaultShardingHandlers.put(ShardingStrategy.HASH, new HashedShardingHandler());
	}
	
	/**
	 * 获取分表表名
	 * @param context
	 */
	public static String getShardingTable(ShardingContext context) {
		if(context.getShardedStrategy() == ShardingStrategy.CUSTOM){
			return shardingHandlers.get(context.getShardedInfo().getHandler()).getShardedTable(context);
		}else {
			return defaultShardingHandlers.get(context.getShardedStrategy()).getShardedTable(context);
		}
	}

	/**
	 * 注册分表Handler
	 */
	public static void registryHandler(String clazzName) {
		ShardingHandler shardingHandler = shardingHandlers.get(clazzName);
		if(shardingHandler!=null) {
			return;
		}
		try {
			Class<?> clazz = Class.forName(clazzName);
			Object handler = clazz.newInstance();
			if(handler instanceof ShardingHandler) {
				shardingHandlers.put(clazzName, (ShardingHandler)handler);
			}else {
				throw new RuntimeException("注册ShardingHandler类型非法：" + handler.getClass());
			}
		} catch (Exception exp) {
			logger.error("注册ShardingHandler出错, clazz={}, 错误信息={}", clazzName, exp.getMessage());
			throw new RuntimeException(exp);
		}
	}

}