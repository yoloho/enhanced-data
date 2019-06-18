package com.yoloho.data.cache.security;

import java.util.TimeZone;

import com.yoloho.common.util.DigestUtil;
import com.yoloho.data.cache.redis.api.RedisService;

/**
 * 利用redis的流控工具类
 * <p>
 * 目前适用一些细粒度的简单流控。
 * <p>
 * 如果做高性能去分布式的流控，还是推荐使用独立的RateLimiter或Semiphore
 * <p>
 * 如果做高性能又兼顾分布式的流控，需要做额外的异步缓冲设计，目前暂无此场景所以没有做相关实现
 * 
 * @author jason<jason@dayima.com> @ Aug 5, 2018
 *
 */
public class FlowControl {
    private static int offset = TimeZone.getDefault().getRawOffset();
    private static final String PREFIX = "core_flowcontrol";
    
    private static String getKey(String key) {
        return DigestUtil.md5(String.format("%s_%s", PREFIX, key));
    }
    
    /**
     * 总量限流
     * 限流范围依靠过期时间
     * 
     * @param key 要注意在redis范围内，名字需要保证唯一
     * @param expire
     * @param redisService
     * @return
     */
    public static long total(String key, long expire, RedisService redisService) {
        return total(key, 1L, expire, redisService);
    }
    
    /**
     * 总量限流
     * 
     * @param key
     * @param num
     * @param expire
     * @param redisService
     * @return
     */
    public static long total(String key, long num, long expire, RedisService redisService) {
        return redisService.getAndInc(getKey(key), 0L, num, expire);
    }
    
    /**
     * 重置
     * 
     * @param key
     * @param redisService
     * @return
     */
    public static void totalReset(String key, RedisService redisService) {
        redisService.delete(getKey(key));
    }
    
    private static String getTimeRelatedKey(String key, int periodInSecond) {
        long p = (System.currentTimeMillis() + offset) / 1000 / periodInSecond;
        return String.format("%s_%d", key, p);
    }
    
    /**
     * 按天限流
     * 
     * @param key
     * @param redisService
     * @return
     */
    public static long day(String key, RedisService redisService) {
        return total(getTimeRelatedKey(key, 86400), 4000L, redisService);
    }
    
    /**
     * 重置
     * 
     * @param key
     * @param redisService
     */
    public static void dayReset(String key, RedisService redisService) {
        totalReset(getTimeRelatedKey(key, 86400), redisService);
    }
    
    /**
     * 按小时限流
     * 
     * @param key
     * @param redisService
     * @return
     */
    public static long hour(String key, RedisService redisService) {
        return total(getTimeRelatedKey(key, 3600), 4000, redisService);
    }
    
    /**
     * 重置
     * 
     * @param key
     * @param redisService
     */
    public static void hourReset(String key, RedisService redisService) {
        totalReset(getTimeRelatedKey(key, 3600), redisService);
    }
    
    /**
     * 按分钟限流
     * 
     * @param key
     * @param redisService
     * @return
     */
    public static long minute(String key, RedisService redisService) {
        return total(getTimeRelatedKey(key, 60), 70, redisService);
    }
    
    /**
     * 重置
     * 
     * @param key
     * @param redisService
     */
    public static void minuteReset(String key, RedisService redisService) {
        totalReset(getTimeRelatedKey(key, 60), redisService);
    }
    
    /**
     * 按指定的秒数为区间限流
     * 
     * @param key
     * @param periodInSecond
     * @param redisService
     * @return
     */
    public static long period(String key, int periodInSecond, RedisService redisService) {
        return total(getTimeRelatedKey(key, periodInSecond), periodInSecond + 10, redisService);
    }
    
    public static void periodReset(String key, int periodInSecond, RedisService redisService) {
        totalReset(getTimeRelatedKey(key, periodInSecond), redisService);
    }
}
