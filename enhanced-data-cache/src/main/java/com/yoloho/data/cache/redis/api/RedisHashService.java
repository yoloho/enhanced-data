package com.yoloho.data.cache.redis.api;

import java.util.Map;
import java.util.Set;

public interface RedisHashService {
	/**
	 * 保存一个对象到一个hash中
	 * 
	 * @param key
	 *            hash的key
	 * @param field
	 *            对象的key
	 * @param value
	 *            对象值
	 */
	public boolean hset(String key, String field, Object value);

	/**
	 * 从一个hash中获取对象
	 * @param <T>
	 * @param key
	 * @param field
	 * @param clazz
	 * @return
	 */
	public <T> T hget(String key, String field, Class<T> clazz);

	/**
	 * 获取hash中的数据个数
	 * 
	 * @param key
	 * @return
	 */
	public Long hlen(String key);

	/**
	 * 删除一个hash中的field
	 * 
	 * @param key
	 * @param field
	 */
	public boolean hdel(String key, String field);

	/**
	 * 判断hash中field是否存在
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public boolean hexists(String key, String field);

	/**
	 * 获取所有field
	 * 
	 * @param key
	 * @return
	 */
	public Set<String>  hkeys(String key);

	/**
	 * 获取全部的filed及value
	 * 
	 * @param key
	 * @return
	 */
	public <T> Map<String, T> hgetall(String key, Class<T> clazz);
}
