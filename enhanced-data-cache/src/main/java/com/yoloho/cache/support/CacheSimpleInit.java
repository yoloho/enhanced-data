package com.yoloho.cache.support;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * Basicly initializing needed under aspectj
 * 
 * @author jason
 *
 */
public class CacheSimpleInit {
    public CacheSimpleInit(String ns) {
        this(ns, null);
    }

    public CacheSimpleInit(String ns, RedisTemplate<String, Object> redisTemplate) {
        if (ns == null || ns.length() == 0) {
            throw new RuntimeException("namespace for cache should not be empty");
        }
        CacheProcessor.setNamespace(ns);
        if (redisTemplate != null) {
            CacheProcessor.setRedisTemplate(redisTemplate);
        }
    }
}