package com.yoloho.enhanced.data.sharding.strategy;

public enum ShardingStrategy {
	
	HASH,				//简单Hash求模分片，需指定分片数量
	TIME_YEAR, 			//按年分片
	TIME_MONTH,			//按月分片
	TIME_DAY,			//按日分片
	CUSTOM;				//自定义分片

}