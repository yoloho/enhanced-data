package com.yoloho.data.dao.sharding.strategy.handler;

import org.apache.commons.lang3.time.FastDateFormat;

import com.yoloho.data.dao.sharding.api.ShardingFactor;
import com.yoloho.data.dao.sharding.strategy.ShardingContext;
import com.yoloho.data.dao.sharding.strategy.ShardingHandler;

public class YearShardingHandler implements ShardingHandler {

    @Override
    public String getShardedTable(ShardingContext context) {
        if (context.getShardedFactors().isEmpty()) {
            throw new RuntimeException("解析分表名称出错:shardedFactor=null");
        }
        ShardingFactor shardedFactor = context.getShardedFactors().get(0);
        if (shardedFactor.getFactorValue() == null) {
            throw new RuntimeException("解析分表名称出错:shardedFactor数据为null");
        }
        if (!(shardedFactor.getFactorValue() instanceof java.util.Date)) {
            throw new RuntimeException("解析分表名称出错:shardedFactor数据类型非java.util.Date !");
        }
        return context.getTableModel() + "_" + FastDateFormat.getInstance("yyyy").format(System.currentTimeMillis());
    }

}
