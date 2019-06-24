package com.yoloho.enhanced.data.cache.redis;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.yoloho.enhanced.data.cache.redis.api.RedisHashService;
import com.yoloho.enhanced.data.cache.redis.support.RedisUtil;

/**
 * redis的hash操作
 * 
 * @author wuzl
 * 
 */
public class RedisHashServiceImpl implements RedisHashService {
	private StringRedisTemplate redisTemplate;

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private RedisSerializer<String> getRedisSerializer() {
		return redisTemplate.getStringSerializer();
	}

	@Override
	public boolean hset(final String key, final String field, final Object value) {
		if (key == null) {
			throw new RuntimeException("key不可以是null");
		}
		if (field == null) {
			throw new RuntimeException("field不可以是null");
		}
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				connection.hSet(getRedisSerializer().serialize(key),
						getRedisSerializer().serialize(field),
						RedisUtil.getBytesFromObject(value));
				return true;
			}
		});
		return result;
	}

	@Override
	public <T> T hget(final String key, final String field, final Class<T> clazz) {
		if (key == null) {
			throw new RuntimeException("key不可以是null");
		}
		if (field == null) {
			throw new RuntimeException("field不可以是null");
		}
		return redisTemplate.execute(new RedisCallback<T>() {
			@Override
			public T doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] value = connection.hGet(
						getRedisSerializer().serialize(key),
						getRedisSerializer().serialize(field));
				if(value==null){
					return null;
				}
				return RedisUtil.getObjectFromBytes(value, clazz);
			}
		});
	}

	@Override
	public Long hlen(final String key) {
		if (key == null) {
			throw new RuntimeException("key不可以是null");
		}
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection)
					throws DataAccessException {
				return connection.hLen(getRedisSerializer().serialize(key));
			}
		});
	}

	@Override
	public boolean hdel(final String key, final String field) {
		if (key == null) {
			throw new RuntimeException("key不可以是null");
		}
		if (field == null) {
			throw new RuntimeException("field不可以是null");
		}
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				connection.hDel(getRedisSerializer().serialize(key),
						getRedisSerializer().serialize(field));
				return true;
			}
		});
	}

	@Override
	public boolean hexists(final String key, final String field) {
		if (key == null) {
			throw new RuntimeException("key不可以是null");
		}
		if (field == null) {
			throw new RuntimeException("field不可以是null");
		}
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				return connection.hExists(getRedisSerializer().serialize(key),
						getRedisSerializer().serialize(field));
			}
		});
	}

	@Override
	public Set<String> hkeys(final String key) {
		if (key == null) {
			throw new RuntimeException("key不可以是null");
		}
		return redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection connection)
					throws DataAccessException {
				Set<String> result = new HashSet<String>();
				Set<byte[]> hKeys = connection.hKeys(getRedisSerializer()
						.serialize(key));
				for (byte[] hkey : hKeys) {
					result.add(getRedisSerializer().deserialize(hkey));
				}
				return result;
			}
		});
	}

	@Override
	public <T> Map<String, T> hgetall(final String key, final Class<T> clazz) {
		if (key == null) {
			throw new RuntimeException("key不可以是null");
		}
		return redisTemplate.execute(new RedisCallback<Map<String, T>>() {
			@Override
			public Map<String, T> doInRedis(RedisConnection connection)
					throws DataAccessException {
				Map<String, T> dto = new HashMap<String, T>();
				Map<byte[], byte[]> all = connection
						.hGetAll(getRedisSerializer().serialize(key));
				Set<byte[]> keys = all.keySet();
				for (byte[] hkey : keys) {
					dto.put(getRedisSerializer().deserialize(hkey),
							RedisUtil.getObjectFromBytes(all.get(hkey), clazz));
				}
				return dto;
			}
		});
	}
}
