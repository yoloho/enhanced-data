package com.yoloho.enhanced.data.cache.redis.api;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;

public interface RedisService {
    
    /**
     *
     * @param key
     * @return null for not existed
     */
    String get(final String key);

    /**
     * Multi get
     *
     * @param keys
     * @return
     */
    List<String> mget(final Collection<String> keys);
    
    /**
     * Default value to 0.<br>
     * Default step to 1.
     * <p>
     * TTL default to be 24 hours
     * 
     * @param key
     * @return
     */
    long increaseAndGet(String key);
    
    /**
     * Default value to 0
     * 
     * @param key
     * @param step
     * @param expireInSeconds 0 for no modification for TTL
     * @return
     */
    long increaseAndGet(String key, long step, int expireInSeconds);

    /**
     * TTL = -1 (Will not be expired)
     * 
     * @param key
     * @param value Any type will be convert into string (json format for objects) 
     * @return
     */
    <T> void set(final String key, final T value);

    /**
     * @param key
     * @param value Any type will be convert into string (json format for objects)
     * @param expireInSeconds -1 for no expiration
     * @return
     */
    <T> void set(String key, T value, int expireInSeconds);
    
    /**
     * Set the key with the value when key is not existed
     *
     * @param key
     * @param value Any type will be convert into string (json format for objects)
     * @param expireInSeconds
     * @return true for success, false for existed key
     */
    <T> boolean setIfAbsent(String key, T value, int expireInSeconds);
    
    /**
     * Multi set
     * 
     * @param map
     */
    void setMulti(Map<String, Object> map);
    
    /**
     * Set expire for a key
     *
     * @param key
     * @param expireInSeconds
     * @return
     */
    void expire(String key, int expireInSeconds);
    
    /**
     * @param key
     * @return
     */
    boolean exists(String key);
    
    /**
     * Publish
     * 
     * @param channel
     * @param msg
     */
    <T> void publish(String channel, T msg);
    
    /**
     *
     * @param key
     */
    void delete(String key);

    /**
     * @param keys
     */
    void delete(Collection<String> keys);
    
    /**
     * Get the list's size
     * 
     * @param key
     * @return
     */
    long listSize(String key);
    
    /**
     * Trim the list to the range
     * 
     * @param key
     * @param start
     * @param end
     */
    void listTrim(String key, int start, int end);

    /**
     * Pop specified range of objects from list
     * 
     * @param key
     * @param start
     * @param end
     * @return
     */
    List<String> listRange(String key, int start, int end);

    /**
     * Pop an object from list
     * 
     * @param key
     * @return null for empty list
     */
    String listPop(String key);

    /**
     * Push an object into list
     * 
     * @param key
     * @param value
     */
    <T> void listPush(String key, T value);

    /**
     * Set a bit in bitmap
     * <p>
     * Default ttl is 30 minutes
     * 
     * @param key
     * @param offset
     * @param value
     * @return
     */
    boolean setBit(String key, long offset, boolean value);
    
    /**
     * Set a bit in bitmap
     * 
     * @param key
     * @param offset
     * @param value
     * @param expireInSeconds
     * @return
     */
    boolean setBit(String key, long offset, boolean value, int expireInSeconds);

    /**
     * Get the bit from bitmap
     * 
     * @param key
     * @param offset
     * @return
     */
    public boolean getBit(String key, long offset);
    
    /**
     * Add a batch
     *
     * @param key
     * @param itemMap item -> score
     */
    void sortedSetMultiAdd(String key, Map<String, Double> itemMap);
    
    /**
     * @param key
     * @param item
     * @param delta
     */
    void sortedSetIncreaseScore(String key, String item, double delta);
    
    /**
     * Get the item's rank
     * 
     * @param key
     * @param item
     * @return
     */
    long sortedSetRank(String key, String item);

    /**
     *
     * @param key
     * @param item
     * @param score
     */
    void sortedSetAdd(String key, String item, double score);
    
    /**
     * Walk the sorted set one way.
     * <p>
     * For elegantly walking please refer to {@link RedisTemplate}.
     * 
     * @param key
     * @param count
     * @return
     */
    List<Pair<String, Double>> sortedSetScan(String key, long count);

    /**
     * Get the items in the range of index
     *
     * @param key
     * @param indexFrom
     * @param indexTo
     * @return
     */
    Set<String> sortedSetRange(String key, int indexFrom, int indexTo);

    /**
     * Get the items in the range of index, order by desc (score)
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    Set<String> sortedSetRangeReverse(String key, long start, long end);

    /**
     * Clear the sorted set
     *
     * @param key
     */
    void sortedSetClear(String key);

    /**
     * Get the size of sorted set
     *
     * @param key
     * @return
     */
    long sortedSetSize(String key);

    /**
     * Get the item's score
     * 
     * @param key
     * @param item
     * @return score
     */
    double sortedSetScore(String key, String item);

    /**
     *
     * @param key
     * @param items
     */
    void sortedSetRemove(String key, String... items);

    /**
     * Remove the items according the range of score
     *
     * @param key
     * @param scoreFrom
     * @param scoreTo
     */
    long sortedSetRemoveByScore(String key, double scoreFrom, double scoreTo);
    
    /**
     * Get a hash's size
     * 
     * @param key
     * @return
     */
    long hashSize(String key);

    /**
     * Fetch a value of specified field of the hash
     * 
     * @param key
     * @param hashKey
     * @return
     */
    String hashGet(String key, String hashKey);

    /**
     * Fetch multiple values
     * 
     * @param key
     * @param hashKeys
     * @return
     */
    List<String> hashMultiGet(String key, Collection<String> hashKeys);

    /**
     * Dump the hash
     * 
     * @param key
     * @return
     */
    Map<String, String> hashGetAll(String key);

    /**
     * @param key
     * @return
     */
    Set<String> hashKeys(String key);
    
    /**
     * Get all the values in the hash
     * 
     * @param key
     * @return
     */
    List<String> hashValues(final String key);
    
    /**
     * 
     * @param key
     * @param hashKey
     * @return
     */
    boolean hashExists(final String key, final String hashKey);
    
    /**
     * Delete item(s) from hash
     * 
     * @param key
     * @param hashKeys
     * @return
     */
    long hashRemove(final String key, final String... hashKeys);
    
    /**
     * Put a value into hash
     * <p>
     * Putting value into hash will not change its ttl.
     * So if you want to renew the ttl you should do it separately.
     * 
     * @param key
     * @param hashKey
     * @param value It always be taken as string
     */
    <T> void hashPut(String key, String hashKey, T value);
    
    /**
     * Put a value into hash if absent
     * <p>
     * Putting value into hash will not change its ttl.
     * So if you want to renew the ttl you should do it separately.
     * 
     * @param key
     * @param hashKey
     * @param value
     */
    <T> void hashPutIfAbsent(String key, String hashKey, T value);
    
    /**
     * Put values into hash.
     * <p>
     * Putting value into hash will not change its ttl.
     * So if you want to renew the ttl you should do it separately.
     * 
     * @param key
     * @param map
     */
    void hashPutAll(final String key, final Map<String, String> map);

    /**
     * Default value to 0.<br>
     * Default step to 1.
     * <p>
     * TTL default to be 24 hours
     * 
     * @param key
     * @param hashKey
     * @return
     */
    long hashIncreaseAndGet(String key, String hashKey);

    /**
     * Default value to 0
     * 
     * @param key
     * @param hashKey
     * @param step
     * @param expireInSeconds expireInSeconds 0 for no modification for TTL
     * @return
     */
    long hashIncreaseAndGet(String key, String hashKey, long step, int expireInSeconds);

}
