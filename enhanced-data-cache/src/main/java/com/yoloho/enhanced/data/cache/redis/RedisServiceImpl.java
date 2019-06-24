package com.yoloho.enhanced.data.cache.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yoloho.enhanced.data.cache.redis.api.RedisService;
import com.yoloho.enhanced.data.cache.redis.support.RedisUtil;
import com.yoloho.enhanced.data.cache.redis.support.SaveRedisBean;
import com.yoloho.enhanced.data.cache.redis.support.ZItem;

/**
 * 对redis操作的service
 *
 * @author wuzl
 */
public class RedisServiceImpl implements RedisService {
    private StringRedisTemplate redisTemplate;

    private StringRedisTemplate redisQueueTemplate;

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        if (redisQueueTemplate == null) {
            redisQueueTemplate = redisTemplate;
        }
    }

    public void setRedisQueueTemplate(StringRedisTemplate redisQueueTemplate) {
        this.redisQueueTemplate = redisQueueTemplate;
    }

    private RedisSerializer<String> getRedisSerializer() {
        return redisTemplate.getStringSerializer();
    }

    /**
     * 保存对象
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, final Object value) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.set(getRedisSerializer().serialize(key),
                        RedisUtil.getBytesFromObject(value));
                return true;
            }
        });
        return result;
    }

    /**
     * 保存字符串
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, final String value) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (value == null) {
            throw new RuntimeException("value不可以是null");
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.set(getRedisSerializer().serialize(key),
                        getRedisSerializer().serialize(value));
                return true;
            }
        });
        return result;
    }

    /**
     * 保存一个字符串一定时间
     *
     * @param key
     * @param value
     * @param saveTime 过期时间
     *                 单位为秒
     * @return
     */
    public boolean set(final String key, final String value, final long saveTime) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (value == null) {
            throw new RuntimeException("value不可以是null");
        }

        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.setEx(getRedisSerializer().serialize(key), saveTime,
                        getRedisSerializer().serialize(value));
                return true;
            }
        });
        return result;
    }

    public long getQueueSize(final String key) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }

        return redisQueueTemplate.opsForList().size(key);
    }

    /**
     * 保存一个对象一定时间
     *
     * @param key
     * @param value
     * @param saveTime 过期时间
     *                 单位为秒
     * @return
     */
    public boolean set(final String key, final Object value, final long saveTime) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.setEx(getRedisSerializer().serialize(key), saveTime,
                        RedisUtil.getBytesFromObject(value));
                return true;
            }
        });
        return result;
    }
    
    /**
     * 当key不存在时设置一个值，并设置过期[采用字符串]
     * @param key
     * @param value
     * @param saveTime 过期时间
     *                 单位为秒
     * @return true为设置成功
     */
    @Override
    public boolean setIfAbsent(String key, String value, long saveTime) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
        if (result && saveTime > 0) {
            redisTemplate.expire(key, saveTime, TimeUnit.SECONDS);
        }
        return result;
    }
    
    /**
     * 当key不存在时设置一个值，并设置过期[采用序列化方式]
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public boolean setNX(String key, String value, long expireTime) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] serialKey = getRedisSerializer().serialize(key);
                boolean flag = connection.setNX(serialKey, getRedisSerializer().serialize(value));
                if(flag && expireTime > 0) {
                    connection.expire(serialKey, expireTime);
                }
                return flag;
            }
        });
        return result;
    }
    
    public boolean setBit(String key, long offset, boolean value) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        return this.setBit(key, offset, value, 30 * 60);
    }
    
    public boolean setBit(String key, long offset, boolean value, long expired) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] serialKey = getRedisSerializer().serialize(key);
                if(connection.setBit(serialKey, offset, value)) {
                	if(expired > 0) {
                    	connection.expire(serialKey, expired);
                	}
                	return true;
                }else {
                	return false;
                }
            }
        });
        return result;
    }
    
    
    public boolean getBit(String key, long offset) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] serialKey = getRedisSerializer().serialize(key);
                return connection.getBit(serialKey, offset);
            }
        });
        return result;
    }

    /**
     * 批量保存
     *
     * @param list
     * @return
     */
    public boolean set(final List<SaveRedisBean> list) {
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                for (SaveRedisBean bean : list) {
                    if (bean.getKey() == null) {
                        throw new RuntimeException("key不可以是null");
                    }
                    BoundValueOperations<String, Object> oper = operations.boundValueOps(bean.getKey());
                    oper.set(RedisUtil.getJsonFromObject(bean.getValue()));

                }
                return operations.exec();
            }
        });
        return true;
    }

    /**
     * 从redis中查询对象
     *
     * @param key
     * @param clazz
     * @return
     */
    public <T> T get(final String key, final Class<T> clazz) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (clazz == null) {
            throw new RuntimeException("clazz不可以是null");
        }
        if (clazz.getName().equals("java.lang.String")) {
            return (T) get(key);
        }
        return redisTemplate.execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] serialKey = getRedisSerializer().serialize(key);
                byte[] value = connection.get(serialKey);
                if (value != null) {
                    return RedisUtil.getObjectFromBytes(value, clazz);
                }
                return null;
            }
        });
    }

    /**
     * 从redis中查询对象
     *
     * @param keys
     * @param clazz
     * @return
     */
    public <T> List<T> mget(final List<String> keys, final Class<T> clazz) {
        if (keys == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (clazz == null) {
            throw new RuntimeException("clazz不可以是null");
        }
        if (clazz.getName().equals("java.lang.String")) {
            return (List<T>) mget(keys);
        }

        return redisTemplate.execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[][] serialKeys = new byte[keys.size()][];
                for (int i = 0; i < keys.size(); i++) {
                    serialKeys[i] = getRedisSerializer().serialize(keys.get(i));
                }

                List<byte[]> values = connection.mGet(serialKeys);
                List<T> dataList = new ArrayList<T>();
                for (byte[] value : values) {
                    if (value == null) {
                        dataList.add(null);
                    } else {
                        dataList.add(RedisUtil.getObjectFromBytes(value, clazz));
                    }
                }
                return dataList;
            }
        });
    }

    /**
     * 从redis中查询出字符串
     *
     * @param key
     * @return
     */
    public String get(final String key) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] serialKey = getRedisSerializer().serialize(key);
                byte[] value = connection.get(serialKey);
                if (value == null) {
                    return null;
                }
                return getRedisSerializer().deserialize(value);
            }
        });
    }

    /**
     * 从redis中查询出字符串
     *
     * @param keys
     * @return
     */
    public List<String> mget(final List<String> keys) {
        if (keys == null) {
            throw new RuntimeException("key不可以是null");
        }
        return redisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[][] serialKeys = new byte[keys.size()][];
                for (int i = 0; i < keys.size(); i++) {
                    serialKeys[i] = getRedisSerializer().serialize(keys.get(i));
                }

                List<byte[]> values = connection.mGet(serialKeys);
                List<String> dataList = new ArrayList<String>();
                for (byte[] value : values) {
                    dataList.add(getRedisSerializer().deserialize(value));
                }
                return dataList;
            }
        });
    }

    /**
     * 删除
     *
     * @param key
     */
    public void delete(String key) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        redisTemplate.delete(key);
    }

    /**
     * 批量删除
     *
     * @param keys
     */
    public void delete(List<String> keys) {
        if (keys == null || keys.isEmpty() == true) {
            return;
        }
        redisTemplate.delete(keys);
    }

    /**
     * 入队列
     *
     * @param key
     * @param value
     */
    public void inQuere(String key, Object value) {
        inQueue(key, RedisUtil.getJsonFromObject(value));
    }

    /**
     * 入队列
     *
     * @param key
     * @param value
     */
    public void inQueue(String key, Object value) {
        inQueue(key, RedisUtil.getJsonFromObject(value));
    }
    /**
     * 入队列
     *
     * @param key
     * @param value
     */
    public void inQuere(String key, String value) {
        inQueue(key, value);
    }
    /**
     * 入队列
     *
     * @param key
     * @param value
     */
    public void inQueue(String key, String value) {
        redisQueueTemplate.opsForList().rightPush(key, value);
    }
    /**
     * 出队列为一个字符串
     *
     * @param key
     * @return
     */
    public String outQueue(String key) {
        return redisQueueTemplate.opsForList().leftPop(key);
    }

    public List<String> outQueueRange(String key, int start, int end) {
        return redisQueueTemplate.opsForList().range(key, start, end);
    }

    public void outQueueTrim(String key, int start, int end) {
        redisQueueTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取key 支持正则
     *
     * @param patternKey
     * @return
     */
    public Set<String> keys(String patternKey) {
        return redisQueueTemplate.keys(patternKey);
    }

    /**
     * 根据正则获取列表值
     *
     * @param patternKey
     * @param clazz
     * @return
     */
    public <T> List<T> getValueListByPatternKey(final String patternKey,
                                                final Class<T> clazz) {
        List<T> rows = new ArrayList<T>();
        Set<String> failSet = this.keys(patternKey);
        for (String key : failSet) {
            rows.add(get(key, clazz));
        }
        return rows;
    }

    /**
     * 批量增加到有序集合
     *
     * @param key       集合的名称
     * @param valueMaps 数据集合， map的key为要使用数据, value为排序的字段
     */
    @Override
    public void zSetAdd(String key, Map<String, ? extends Number> valueMaps) {
        Set<ZSetOperations.TypedTuple<String>> list = new HashSet<ZSetOperations.TypedTuple<String>>();
        for (String s : valueMaps.keySet()) {
            list.add(new DefaultTypedTuple<String>(s, valueMaps.get(s).doubleValue()));
        }
        redisTemplate.opsForZSet().add(key, list);
    }
    
    @Override
    public void zSetInc(String key, String value, double delta) {
        redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }
    
    @Override
    public long zSetRank(String key, String value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }
    
    /**
     * 批量删除有序集合中的数据
     *
     * @param key    集合的名称
     * @param values 数据集合
     */
    public void zSetRemove(String key, List<? extends Object> values) {
        redisTemplate.opsForZSet().remove(key, values.toArray());
    }

    /**
     * 增加到有序集合
     *
     * @param key            集合的名称
     * @param value          数据的值
     * @param sortFieldValue 作为排序的字段值
     */
    public void zSetAdd(String key, String value, Object sortFieldValue) {
        redisTemplate.opsForZSet().add(key, value, Double.parseDouble(sortFieldValue.toString()));
    }
    
    @Override
    public List<ZItem> zScan(final String key, final long count) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        List<Tuple> list = redisTemplate.execute(new RedisCallback<List<Tuple>>() {
            
            @Override
            public List<Tuple> doInRedis(RedisConnection connection) {
                byte[] hashKeySerial = getRedisSerializer().serialize(key);
                Cursor<Tuple> result = connection.zScan(hashKeySerial, ScanOptions.scanOptions().count(count).build());
                List<Tuple> values = Lists.newArrayList();
                if (result.hasNext()) {
                    values.add(result.next());
                }
                return values;
            }
        });
        if (list == null) {
            return Collections.emptyList();
        }
        List<ZItem> result = new ArrayList<>(list.size());
        for (Tuple tuple : list) {
            result.add(new ZItem(tuple.getValue(), tuple.getScore()));
        }
        return result;
    }

    /**
     * 取得有序集合的数据,按照排序字段升序
     *
     * @param key   集合的名称
     * @param start 开始位置
     * @param end   截止位置
     * @return
     */
    public Set<String> zSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 取得有序集合的数据,按照排序字段降序
     *
     * @param key   集合的名称
     * @param start 开始位置
     * @param end   截止位置
     * @return
     */
    public Set<String> zSetReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public void zSetClear(String key) {
        long size = redisTemplate.opsForZSet().size(key).longValue();
        redisTemplate.opsForZSet().removeRange(key, 0l, size);
    }

    public long zSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    @Override
    public int zSetScore(String key, Object value) {
        Double d = redisTemplate.opsForZSet().score(key, value.toString());
        return d == null ? 0 : d.intValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T outQueueNoBlack(String key, Class<T> clazz) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (clazz == null) {
            throw new RuntimeException("clazz不可以是null");
        }
        String json = redisQueueTemplate.opsForList().leftPop(key, 1, TimeUnit.SECONDS);
        if (json == null) {
            return null;
        }
        if (clazz.getName().equals("java.lang.String")) {
            return (T) json;
        }
        return JSON.parseObject(json, clazz);
    }

    @Override
    public long zSetRemoveByScore(final String key, final long start, final long end) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, start, end);
    }
    
    @Override
    public long zSetRemoveByScore(final String key, final double start, final double end) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, start, end);
    }

    @Override
    public Long getAndInc(final String key, final Long defaultValue, final Long step, final Long timeoutInSecond) {
        try {
            return redisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection)
                        throws DataAccessException {
                    byte[] serialKey = getRedisSerializer().serialize(key);
                    connection.setNX(serialKey, getRedisSerializer().serialize(defaultValue + ""));
                    connection.expire(serialKey, timeoutInSecond);
                    return connection.incrBy(serialKey, step);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue + 1;
        }
    }

    @Override
    public Long getAndInc(final String key, final Long defaultValue) {
        return getAndInc(key, defaultValue, 1L, 60 * 30L);
    }
    
	@Override
	public Long incrAndGet(String key, long defaultValue, long step, long expireSeconds) {
        try {
            return redisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection)
                        throws DataAccessException {
                    byte[] serialKey = getRedisSerializer().serialize(key);
                    connection.setNX(serialKey, getRedisSerializer().serialize(String.valueOf(defaultValue)));
                    connection.expire(serialKey, expireSeconds);
                    return connection.incrBy(serialKey, step);
                }
            });
        } catch (Exception e) {
        	throw new RuntimeException("redis命令出错【incrAndGet】", e);
        }
	}

    @Override
    public Long hGetAndInc(final String hashKey, final String key, final Long defaultValue) {
        return hGetAndInc(hashKey, key, defaultValue, 1L, 60 * 30L);
    }

    @Override
    public Long hGetAndInc(final String hashKey, final String key, final Long defaultValue, final Long step, final Long timeoutInSecond) {
        try {
            return redisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection)
                        throws DataAccessException {
                    byte[] hashKeySerial = getRedisSerializer().serialize(hashKey);
                    byte[] keySerial = getRedisSerializer().serialize(key);
                    connection.hSetNX(hashKeySerial, keySerial, getRedisSerializer().serialize("" + defaultValue));
                    if(timeoutInSecond > 0) {
                        connection.expire(hashKeySerial, timeoutInSecond);
                    }
                    return connection.hIncrBy(hashKeySerial, keySerial, step);
                }
            });
        } catch (Exception e) {
            return defaultValue + 1;
        }
    }
    
    @Override
    public void expire(final String key, final Long timeout) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    @Override
    public void publish(final String channel, String msg) {
        if (channel == null) {
            throw new RuntimeException("channel不可以是null");
        }
        redisTemplate.convertAndSend(channel, msg);
    }

    @Override
    public boolean exists(final String key) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        return redisTemplate.hasKey(key);
    }

    @Override
    public void hset(final String key, final String field, final String value){
        hset(key, field, value, 60 * 30L);
    }
    
    @Override
    public long hDel(final String key, final String... fields) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (fields == null || fields.length == 0) {
            throw new RuntimeException("field不可以是null或空");
        }
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] hashKeySerial = serializer.serialize(key);
                byte[][] bytesFields = new byte[fields.length][];
                for (int i = 0; i < fields.length; i++) {
                    bytesFields[i] = serializer.serialize(fields[i]);
                }
                return connection.hDel(hashKeySerial, bytesFields);
            }
        });
    }
    
    @Override
    public boolean hExists(final String key, final String field) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (field == null) {
            throw new RuntimeException("field不可以是null");
        }
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) {
                byte[] hashKeySerial = getRedisSerializer().serialize(key);
                byte[] keySerial = getRedisSerializer().serialize(field);
                return connection.hExists(hashKeySerial, keySerial);
            }
        });
    }
    
    @Override
    public List<String> hVals(final String key) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
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
    public Set<String> hKeys(final String key) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        return redisTemplate.execute(new RedisCallback<Set<String>>() {
            @Override
            public Set<String> doInRedis(RedisConnection connection) {
                byte[] hashKeySerial = getRedisSerializer().serialize(key);
                Set<byte[]> results = connection.hKeys(hashKeySerial);
                Set<String> keys = Sets.newConcurrentHashSet();
                if (results != null && results.size() > 0) {
                    for (byte[] bytes : results) {
                        keys.add(new String(bytes));
                    }
                }
                return keys;
            }
        });
    }

    @Override
    public void hset(final String key, final String field, final String value, final Long timeoutInSecond) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (field == null) {
            throw new RuntimeException("field不可以是null");
        }
        redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) {
                byte[] hashKeySerial = getRedisSerializer().serialize(key);
                byte[] keySerial = getRedisSerializer().serialize(field);
                connection.hSet(hashKeySerial, keySerial, getRedisSerializer().serialize("" + value));
                if(timeoutInSecond > 0) {
                    connection.expire(hashKeySerial, timeoutInSecond);
                }
                return true;
            }
        });
    }
    
    @Override
    public void hMSet(final String key, final Map<String, String> map) {
        hMSet(key, map, 60 * 10L);
    }
    
    @Override
    public void hMSet(final String key, final Map<String, String> map, final Long timeoutInSecond) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (map == null) {
            throw new RuntimeException("map不可以是null");
        }
        redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] hashKeySerial = serializer.serialize(key);
                Map<byte[], byte[]> bytesMap = Maps.newConcurrentMap();
                for (Entry<String, String> entry : map.entrySet()) {
                    bytesMap.put(serializer.serialize(entry.getKey()), serializer.serialize(entry.getValue()));
                }
                connection.hMSet(hashKeySerial, bytesMap);
                if (timeoutInSecond > 0) {
                    connection.expire(hashKeySerial, timeoutInSecond);
                }
                return true;
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Object> hmget(final String key, final Collection<? extends Object> fields) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (fields == null) {
            throw new RuntimeException("field不可以是null");
        }
        return redisTemplate.opsForHash().multiGet(key, (Collection<Object>) fields);
    }
    
    @Override
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public String hget(final String key, final String field) {
        if (key == null) {
            throw new RuntimeException("key不可以是null");
        }
        if (field == null) {
            throw new RuntimeException("field不可以是null");
        }
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) {
                byte[] hashKeySerial = getRedisSerializer().serialize(key);
                byte[] keySerial = getRedisSerializer().serialize(field);
                byte[] value = connection.hGet(hashKeySerial, keySerial);
                if (value == null) {
                    return null;
                }
                try {
                    return new String(value, "UTF-8");
                } catch (Exception ex) {
                    return new String(value);
                }
            }
        });
    }
    @Override
    public Long hLen(final String key) {
        try {
            return (Long) redisTemplate.execute(new RedisCallback<Long>() {
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] serialKey = getRedisSerializer().serialize(key);
                    return connection.hLen(serialKey);
                }
            });
        } catch (Exception var6) {
            var6.printStackTrace();
            return null;
        }
    }

}
