package com.yoloho.enhanced.data.cache.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.yoloho.enhanced.data.cache.redis.api.RedisService;
import com.yoloho.enhanced.data.cache.redis.support.RedisUtil;

/**
 * Redis utility
 *
 * @author wuzl
 * 
 * Restructured by
 * @author jason
 */
public class RedisServiceImpl implements RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class.getSimpleName());
    private StringRedisTemplate redisTemplate;

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private RedisSerializer<String> getRedisSerializer() {
        return redisTemplate.getStringSerializer();
    }
    
    @Override
    public String get(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<String> mget(Collection<String> keys) {
        Preconditions.checkNotNull(keys, "Key should not be null");
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }
        return redisTemplate.opsForValue().multiGet(keys);
    }
    
    @Override
    public long increaseAndGet(String key) {
        return increaseAndGet(key, 1, 86400);
    }
    
    @Override
    public long increaseAndGet(String key, long step, int expireInSecond) {
        try {
            return redisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection)
                        throws DataAccessException {
                    byte[] serialKey = getRedisSerializer().serialize(key);
                    long result = connection.incrBy(serialKey, step);
                    if (expireInSecond > 0) {
                        connection.expire(serialKey, expireInSecond);
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            logger.error("Redis operation failed", e);
        }
        return 0;
    }

    @Override
    public void delete(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        redisTemplate.delete(key);
    }

    @Override
    public void delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty() == true) {
            return;
        }
        redisTemplate.delete(keys);
    }
    
    @Override
    public void expire(String key, int expireInSecond) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkArgument(expireInSecond > 0, "expireInSecond should be greater than 0");
        redisTemplate.expire(key, expireInSecond, TimeUnit.SECONDS);
    }

    @Override
    public <T> void publish(String channel, T msg) {
        Preconditions.checkNotNull(channel, "Channel should not be null");
        Preconditions.checkNotNull(msg, "Message should not be null");
        redisTemplate.convertAndSend(channel, msg);
    }

    @Override
    public boolean exists(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.hasKey(key);
    }
    
    @Override
    public <T> void set(String key, T value) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(value, "Value should not be null");
        redisTemplate.execute(new RedisCallback<Void>() {
            public Void doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.set(getRedisSerializer().serialize(key),
                        RedisUtil.toBytes(value));
                return null;
            }
        });
    }
    
    @Override
    public <T> void set(final String key, T value, final int expireInSeconds) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(value, "Value should not be null");
        redisTemplate.execute(new RedisCallback<Void>() {
            public Void doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.setEx(getRedisSerializer().serialize(key), expireInSeconds,
                        RedisUtil.toBytes(value));
                return null;
            }
        });
    }
    
    @Override
    public void setMulti(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        Map<String, String> newMap = new HashMap<String, String>(map.size());
        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                logger.warn("Ignore null key or value pair: {} => {}", entry.getKey(), entry.getValue());
                continue;
            }
            newMap.put(entry.getKey(), RedisUtil.toString(entry.getValue()));
        }
        if (newMap.isEmpty()) {
            return;
        }
        redisTemplate.opsForValue().multiSet(newMap);
    }
    
    @Override
    public <T> boolean setIfAbsent(String key, T value, int expireInSeconds) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(value, "Value should not be null");
        boolean result = redisTemplate.opsForValue().setIfAbsent(key, RedisUtil.toString(value));
        if (result && expireInSeconds > 0) {
            redisTemplate.expire(key, expireInSeconds, TimeUnit.SECONDS);
        }
        return result;
    }
    
    @Override
    public boolean setBit(String key, long offset, boolean value) {
        return setBit(key, offset, value, 30 * 60);
    }
    
    @Override
    public boolean setBit(String key, long offset, boolean value, int expireInSeconds) {
        Preconditions.checkNotNull(key, "Key should not be null");
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] serialKey = getRedisSerializer().serialize(key);
                if(connection.setBit(serialKey, offset, value)) {
                    if(expireInSeconds > 0) {
                        connection.expire(serialKey, expireInSeconds);
                    }
                    return true;
                }else {
                    return false;
                }
            }
        });
        return result;
    }
    
    @Override
    public boolean getBit(String key, long offset) {
        Preconditions.checkNotNull(key, "Key should not be null");
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] serialKey = getRedisSerializer().serialize(key);
                return connection.getBit(serialKey, offset);
            }
        });
        return result;
    }
    
    /////////////////// List //////////////////////
    
    @Override
    public long listSize(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.opsForList().size(key);
    }
    
    @Override
    public <T> void listPush(String key, T value) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(value, "Value should not be null");
        redisTemplate.opsForList().rightPush(key, RedisUtil.toString(value));
    }

    @Override
    public String listPop(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public List<String> listRange(String key, int start, int end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public void listTrim(String key, int start, int end) {
        redisTemplate.opsForList().trim(key, start, end);
    }
    
    /////////////////// Sorted Set //////////////////////
    
    @Override
    public void sortedSetMultiAdd(String key, Map<String, Double> itemMap) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkArgument(itemMap != null && !itemMap.isEmpty(), "itemMap should not be null");
        Set<ZSetOperations.TypedTuple<String>> list = new HashSet<ZSetOperations.TypedTuple<String>>();
        for (String s : itemMap.keySet()) {
            list.add(new DefaultTypedTuple<String>(s, itemMap.get(s)));
        }
        redisTemplate.opsForZSet().add(key, list);
    }
    
    @Override
    public void sortedSetIncreaseScore(String key, String item, double delta) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(item, "Item should not be null");
        redisTemplate.opsForZSet().incrementScore(key, item, delta);
    }
    
    @Override
    public long sortedSetRank(String key, String item) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(item, "Item should not be null");
        return redisTemplate.opsForZSet().rank(key, item);
    }
    
    @Override
    public void sortedSetRemove(String key, String... items) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(items, "Items should not be null");
        if (items.length < 1) {
            return;
        }
        redisTemplate.opsForZSet().remove(key, (Object[])items);
    }

    @Override
    public void sortedSetAdd(String key, String item, double score) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(item, "Item should not be null");
        redisTemplate.opsForZSet().add(key, item, score);
    }
    
    @Override
    public List<Pair<String, Double>> sortedSetScan(String key, long count) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Cursor<TypedTuple<String>> cursor = redisTemplate.opsForZSet().scan(key, ScanOptions.scanOptions().count(count).build());
        List<Pair<String, Double>> list = new ArrayList<>(4);
        try {
            while (cursor.hasNext()) {
                TypedTuple<String> item = cursor.next();
                list.add(Pair.of(item.getValue(), item.getScore()));
            }
        } finally {
            try {
                cursor.close();
            } catch (IOException e) {
            }
        }
        return list;
    }

    @Override
    public Set<String> sortedSetRange(String key, int indexFrom, int indexTo) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.opsForZSet().range(key, indexFrom, indexTo);
    }

    @Override
    public Set<String> sortedSetRangeReverse(String key, long start, long end) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    @Override
    public void sortedSetClear(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        redisTemplate.opsForZSet().removeRange(key, 0l, sortedSetSize(key));
    }

    @Override
    public long sortedSetSize(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.opsForZSet().size(key);
    }

    @Override
    public double sortedSetScore(String key, String item) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(item, "Item should not be null");
        Double d = redisTemplate.opsForZSet().score(key, item);
        return d == null ? 0 : d;
    }

    @Override
    public long sortedSetRemoveByScore(String key, double scoreFrom, double scoreTo) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.opsForZSet().removeRangeByScore(key, scoreFrom, scoreTo);
    }
    
    /////////////////// Hash //////////////////////

    @Override
    public long hashIncreaseAndGet(String key, String hashKey) {
        return hashIncreaseAndGet(key, hashKey, 1, 86400);
    }

    @Override
    public long hashIncreaseAndGet(String key, String hashKey, long step, int expireInSeconds) {
        long result = redisTemplate.opsForHash().increment(key, hashKey, step);
        if (expireInSeconds > 0) {
            redisTemplate.expire(hashKey, expireInSeconds, TimeUnit.SECONDS);
        }
        return result;
    }
    
    @Override
    public long hashRemove(String key, String... hashKeys) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkArgument(hashKeys != null && hashKeys.length > 0, "HashKey should not be empty");
        return redisTemplate.opsForHash().delete(key, (Object[])hashKeys);
    }
    
    @Override
    public boolean hashExists(String key, String hashKey) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(hashKey, "HashKey should not be null");
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }
    
    @Override
    public List<String> hashValues(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        return redisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection) {
                byte[] hashKeySerial = getRedisSerializer().serialize(key);
                List<byte[]> results = connection.hVals(hashKeySerial);
                List<String> vals = Lists.newArrayList();
                if (results != null && results.size() > 0) {
                    for (byte[] bytes : results) {
                        vals.add(new String(bytes));
                    }
                }
                return vals;
            }
        });
    }
    
    @Override
    public <T> void hashPut(String key, String hashKey, T value) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(hashKey, "HashKey should not be null");
        Preconditions.checkNotNull(value, "Value should not be null");
        redisTemplate.opsForHash().put(key, hashKey, RedisUtil.toString(value));
    }
    
    @Override
    public <T> void hashPutIfAbsent(String key, String hashKey, T value) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(hashKey, "HashKey should not be null");
        Preconditions.checkNotNull(value, "Value should not be null");
        redisTemplate.opsForHash().putIfAbsent(key, hashKey, RedisUtil.toString(value));
    }
    
    @Override
    public void hashPutAll(String key, Map<String, String> map) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(map, "ValueMap should not be null");
        if (map.isEmpty()) {
            return;
        }
        redisTemplate.opsForHash().putAll(key, map);
    }
    
    @Override
    public Set<String> hashKeys(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Set<Object> result = redisTemplate.opsForHash().keys(key);
        if (result == null || result.isEmpty()) {
            return Collections.emptySet();
        }
        return result.stream()
                .map(RedisUtil::toString)
                .collect(Collectors.toSet());
    }
    
    @Override
    public String hashGet(String key, String hashKey) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(hashKey, "HashKey should not be null");
        Object result = redisTemplate.opsForHash().get(key, hashKey);
        if (result == null) {
            return null;
        }
        return result.toString();
    }
    
    @Override
    public List<String> hashMultiGet(String key, Collection<String> hashKeys) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Preconditions.checkNotNull(hashKeys, "HashKey should not be null");
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<Object> list = redisTemplate.opsForHash().multiGet(key, (Collection)hashKeys);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(RedisUtil::toString)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, String> hashGetAll(String key) {
        Preconditions.checkNotNull(key, "Key should not be null");
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> newMap = new HashMap<>(map.size());
        for (Entry<Object, Object> entry : map.entrySet()) {
            newMap.put(RedisUtil.toString(entry.getKey()), 
                    RedisUtil.toString(entry.getValue()));
        }
        return newMap;
    }
    
    @Override
    public long hashSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

}
