package com.yoloho.data.dao.sharding.support.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.yoloho.data.dao.sharding.api.ShardingFactor;

public class ShardingFactorUtils {

    public static List<ShardingFactor> build(String shardParam, Object shardValue) {
        List<ShardingFactor> factors = Lists.newArrayListWithCapacity(1);
        factors.add(new ShardingFactor(shardParam, shardValue));
        return factors;
    }

    public static List<ShardingFactor> buildAll(Map<String, Object> factorMap) {
        if (factorMap == null || factorMap.isEmpty()) {
            throw new RuntimeException("Partition factor can not be null");
        } else {
            List<ShardingFactor> factors = Lists.newArrayListWithCapacity(factorMap.size());
            Set<String> paramSet = factorMap.keySet();
            Iterator<String> itor = paramSet.iterator();
            while (itor.hasNext()) {
                String shardParam = itor.next();
                factors.add(new ShardingFactor(shardParam, factorMap.get(shardParam)));
            }
            return factors;
        }
    }

}