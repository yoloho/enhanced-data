package com.yoloho.data.cache.redis.api;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yoloho.data.cache.redis.support.SaveRedisBean;
import com.yoloho.data.cache.redis.support.ZItem;

public interface RedisService {

    /**
     * 保存对象
     *
     * @param key
     * @param value
     * @return
     */
    boolean set(final String key, final Object value);

    /**
     * 保存字符串
     *
     * @param key
     * @param value
     * @return
     */
    boolean set(final String key, final String value);

    /**
     * 保存一个字符串一定时间
     *
     * @param key
     * @param value
     * @param saveTime 过期时间 单位为秒
     * @return
     */
    boolean set(final String key, final String value, final long saveTime);

    /**
     * 保存一个对象一定时间
     *
     * @param key
     * @param value
     * @param saveTime 过期时间 单位为秒
     * @return
     */
    boolean set(final String key, final Object value, final long saveTime);
    
    /**
     * 当key不存在时设置一个值，并设置过期
     *
     * @param key
     * @param value
     * @param saveTime 过期时间
     *                 单位为秒
     * @return true为设置成功
     */
    boolean setIfAbsent(String key, String value, long saveTime);
    
    /**
     * 当key不存在时设置一个值，并设置过期[采用序列化方式]
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public boolean setNX(String key, String value, long expireTime);
    
    /**
     * bitmap位设置
     * @param key
     * @param offset
     * @param value
     * @return
     */
    boolean setBit(String key, long offset, boolean value);
    
    /**
     * bitmap位设置
     * @param key
     * @param offset
     * @param value
     * @param expired	单位：秒
     * @return
     */
    boolean setBit(String key, long offset, boolean value, long expired);

    /**
     * 批量保存
     *
     * @param list
     * @return
     */
    boolean set(final List<SaveRedisBean> list);

    /**
     * bitmap位查询
     * @param key
     * @param offset
     * @return
     */
    public boolean getBit(String key, long offset);
    
    /**
     * 从redis中查询对象
     *
     * @param key
     * @param clazz
     * @return
     */
    <T> T get(final String key, final Class<T> clazz);

    /**
     * 从redis中查询对象
     *
     * @param keys
     * @param clazz
     * @return
     */
    <T> List<T> mget(final List<String> keys, final Class<T> clazz);

    /**
     * 从redis中查询出字符串
     *
     * @param key
     * @return
     */
    String get(final String key);

    /**
     * 从redis中查询出字符串
     *
     * @param keys
     * @return
     */
    List<String> mget(final List<String> keys);
    
    /**
     * 查询map所有keys
     * @param key
     * @return
     */
    Set<String> hKeys(final String key);
    
    /**
     * 查询所有包含的values
     * @param key
     * @return
     */
    List<String> hVals(final String key);
    
    /**
     * 查询某key是否存在
     * @param key
     * @param field
     * @return
     */
    boolean hExists(final String key, final String field);
    
    /**
     * 删除某子项
     * @param key
     * @param field
     * @return
     */
    long hDel(final String key, final String... fields);
    
    /**
     * 保存hash对象
     * @param key
     * @param field
     * @param value
     */
    void hset(String key, String field, String value, Long timeoutInSecond);
    /**
     * 保存hash对象, 默认超时30分钟
     * @param key
     * @param field
     * @param value
     */

    void hset(final String key, final String field, final String value);
    
    /**
     * 批量set
     * @param key
     * @param map
     */
    void hMSet(final String key, final Map<String, String> map);
    void hMSet(final String key, final Map<String, String> map, final Long timeoutInSecond);

    Map<Object, Object> hGetAll(String key);
    
    /**
     * 根据key获取hash对象
     * @param key
     * @param field
     */
    String hget(String key, String field);
    
    List<Object> hmget(final String key, final Collection<? extends Object> fields);

    Long hLen(final String key);
    /**
     * 删除
     *
     * @param key
     */
    void delete(String key);

    /**
     * 批量删除
     *
     * @param keys
     */
    void delete(List<String> keys);

    /**
     * 入队列
     *
     * @param key
     * @param value
     */
    void inQueue(String key, Object value);

    /**
     * 入队列
     *
     * @param key
     * @param value
     */
    void inQueue(String key, String value);

    /**
     * 出队列为一个字符串
     *
     * @param key
     * @return
     */
    String outQueue(final String key);

    List<String> outQueueRange(final String key, int start, int end);

    void outQueueTrim(final String key, int start, int end);

    /**
     * 出队列为一个对象非阻塞
     *
     * @param key
     * @param clazz
     * @return
     */
    <T> T outQueueNoBlack(final String key, Class<T> clazz);

    /**
     * 获取key 支持正则
     *
     * @param patternKey
     * @return
     */
    Set<String> keys(String patternKey);

    /**
     * 根据正则获取列表值
     *
     * @param patternKey
     * @param clazz
     * @return
     */
    <T> List<T> getValueListByPatternKey(final String patternKey,
                                                final Class<T> clazz);

    /**
     * 获取队列长度
     *
     * @param key
     * @return
     */
    long getQueueSize(final String key);

    /**
     * 批量增加到有序集合
     *
     * @param key       集合的名称
     * @param valueMaps 数据集合， map的key为要使用数据, value为排序的字段
     */
    void zSetAdd(String key, Map<String, ? extends Number> valueMaps);
    
    /**
     * 指定的值做分数操作
     * @param key
     * @param value
     * @param delta
     */
    void zSetInc(String key, String value, double delta);
    
    long zSetRank(String key, String value);

    /**
     * 增加到有序集合
     *
     * @param key            集合的名称
     * @param value          数据的值
     * @param sortFieldValue 作为排序的字段值
     */
    void zSetAdd(String key, String value, Object sortFieldValue);
    
    List<ZItem> zScan(final String key, final long count);

    /**
     * 取得有序集合的数据
     *
     * @param key   集合的名称
     * @param start 开始位置
     * @param end   截止位置
     * @return
     */
    Set<String> zSetRange(String key, long start, long end);

    /**
     * 取得有序集合的数据,按照排序字段降序
     *
     * @param key   集合的名称
     * @param start 开始位置
     * @param end   截止位置
     * @return
     */
    Set<String> zSetReverseRange(String key, long start, long end);

    /**
     * 清空有序集合
     *
     * @param key
     */
    void zSetClear(String key);

    /**
     * 读取有序集合的size
     *
     * @param key
     * @return
     */
    long zSetSize(String key);

    /**
     * 查询有序集合项的分数
     * @param key 集合名
     * @param value 项标识
     * @return 分值
     */
    int zSetScore(String key, Object value);

    /**
     * 批量删除有序集合中的数据
     *
     * @param key    集合的名称
     * @param values 数据集合
     */
    void zSetRemove(String key, List<? extends Object> values);

    /**
     * 根据score批量删除
     *
     * @param key
     * @param start
     * @param end
     */
    long zSetRemoveByScore(String key, long start, long end);
    long zSetRemoveByScore(String key, double start, double end);

    /**
     * 获取并增长key  如果没有设置默认值
     * 超时时间默认 30分钟
     * @param key
     * @param defaultValue
     * @return
     */
    Long getAndInc(String key, Long defaultValue);


    /**
     *取并增长key  如果没有设置默认值
     * @param key
     * @param defaultValue
     * @param timeoutInSecond
     * @return
     */
    Long getAndInc(final String key, final Long defaultValue, final Long step, final Long timeoutInSecond);
    
    /**
     * 增长key并获取值， 如果没有设置默认值
     * @param key
     * @param defaultValue
     * @param step
     * @param expireSeconds
     * @return
     */
    Long incrAndGet(final String key, final long defaultValue, final long step, final long expireSeconds);

    /**
     * hash获取并增长key，步长1, 默认超时30分钟
     * @param hashKey
     * @param key
     * @param defaultValue
     * @return
     */
    Long hGetAndInc(final String hashKey, final String key, final Long defaultValue);

    /**
     * hash获取并增长key, 步长1
     * @param hashKey
     * @param key
     * @param defaultValue
     * @param timeoutInSecond
     * @return
     */
    Long hGetAndInc(final String hashKey, final String key, final Long defaultValue, final Long step, final Long timeoutInSecond);

    /**
     * 设置key的过期时间
     *
     * @param key  redisKey
     * @param timeout 保存时间秒
     * @return
     */
    void expire(String key, Long timeout);
    
    /**
     * 确定一个Key是否存在
     * @param key
     * @return 存在：true;否则false
     */
    boolean exists(String key);
    
    /**
     * 
     * @param channel
     * @param msg
     */
    void publish(final String channel, String msg);
}
