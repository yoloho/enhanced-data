package com.yoloho.enhanced.data.cache.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Maps;
import com.yoloho.enhanced.data.cache.redis.api.RedisService;

/**
 * @author mei
 * @date 26/12/2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:context-redis.xml")
//@Ignore
public class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void testBit() {
        String key = "test_bit_1024";
        redisService.delete(key);
        assertFalse(redisService.getBit(key, 1024));
        redisService.setBit(key, 1024, true);
        assertTrue(redisService.getBit(key, 1024));
        redisService.setBit(key, 1024, false);
        assertFalse(redisService.getBit(key, 1024));
        assertFalse(redisService.getBit(key, 1023));
        redisService.delete(key);
    }

    
    @Test
    public void keysTest() {
        String key = "test_ut_ddddd";
        redisService.delete(key);
        assertNull(redisService.get(key));
        assertNull(redisService.get(key, Integer.class));
        redisService.set(key, "ddd", 333);
        assertEquals("ddd", redisService.get(key));
        redisService.set(key, 123);
        assertEquals("123", redisService.get(key));
        assertEquals(new Integer(123), redisService.get(key, Integer.class));
        redisService.set(key, false);
        assertEquals("false", redisService.get(key));
        
        List<String> list = redisService.mget(Arrays.asList(key));
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("false", list.get(0));
        
        List<Boolean> list1 = redisService.mget(Arrays.asList(key), Boolean.class);
        assertNotNull(list1);
        assertEquals(1, list1.size());
        assertFalse(list1.get(0));
        
        assertFalse(redisService.setIfAbsent(key, "ddd", 3333));
        assertEquals("false", redisService.get(key));
        redisService.delete(key);
        assertTrue(redisService.setIfAbsent(key, "ddd", 3333));
        assertEquals("ddd", redisService.get(key));
        Map<String, Object> map = Maps.newConcurrentMap();
        map.put(key, "eee");
        redisService.setMulti(map);
        assertEquals("eee", redisService.get(key));
        assertTrue(redisService.exists(key));
        redisService.delete(key);
        assertFalse(redisService.exists(key));
        assertNull(redisService.get(key));
        
        assertEquals(1, redisService.increaseAndGet(key));
        assertEquals(2, redisService.increaseAndGet(key));
        assertEquals(4, redisService.increaseAndGet(key, 2, 3333));
        redisService.delete(key);
        assertEquals(2, redisService.increaseAndGet(key, 2, 3333));
        redisService.delete(key);
    }

    @Test
    public void listTest() {
        String queue = "test_ut_qqqqqq";
        redisService.delete(queue);
        assertEquals(0, redisService.listSize(queue));
        redisService.listPush(queue, "test");
        redisService.listPush(queue, 123);
        redisService.listPush(queue, 122);
        assertEquals(3, redisService.listSize(queue));
        assertEquals("test", redisService.listPop(queue));
        assertEquals(2, redisService.listSize(queue));
        assertEquals("123", redisService.listPop(queue));
        assertEquals(new Integer(122), redisService.listPop(queue, Integer.class));
        redisService.listPush(queue, "test");
        redisService.listPush(queue, 123);
        assertEquals(2, redisService.listSize(queue));
        
        List<String> list = redisService.listRange(queue, 0, 0);
        assertEquals(1, list.size());
        assertEquals(2, redisService.listSize(queue));
        assertEquals("test", list.get(0));
        
        List<Integer> list1 = redisService.listRange(queue, 0, 1, Integer.class);
        assertNull(list1.get(0));
        assertEquals(new Integer(123), list1.get(1));
        
        redisService.listTrim(queue, 1, -1);
        assertEquals(1, redisService.listSize(queue));
        assertEquals("123", redisService.listPop(queue));
        redisService.delete(queue);
    }
    
    @Test
    public void hashTest() {
        String key = "test_ut_hash";
        redisService.delete(key);
        assertEquals(0, redisService.hashSize(key));
        redisService.hashPut(key, "a", 2);
        assertEquals("2", redisService.hashGet(key, "a"));
        redisService.hashPut(key, "a", false);
        assertEquals("false", redisService.hashGet(key, "a"));
        redisService.hashPut(key, "a", "1");
        assertEquals("1", redisService.hashGet(key, "a"));
        redisService.hashPutIfAbsent(key, "a", "2");
        assertEquals("1", redisService.hashGet(key, "a"));
        
        List<String> list = redisService.hashMultiGet(key, Arrays.asList("a"));
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0));
        
        Map<String, String> map = redisService.hashGetAll(key);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));
        
        list = redisService.hashValues(key);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0));
        
        assertTrue(redisService.hashExists(key, "a"));
        assertFalse(redisService.hashExists(key, "b"));
        redisService.hashRemove(key, "a");
        assertFalse(redisService.hashExists(key, "a"));
        
        map.clear();
        map.put("a", "1");
        map.put("b", "2");
        redisService.hashPutAll(key, map);
        assertTrue(redisService.hashExists(key, "a"));
        assertTrue(redisService.hashExists(key, "b"));
        assertEquals(3, redisService.hashIncreaseAndGet(key, "b"));
        assertEquals(5, redisService.hashIncreaseAndGet(key, "b", 2, 0));
        
        redisService.delete(key);
    }
    
    @Test
    public void sortedSetTest() {
        String key = "test_ut_zset";
        redisService.delete(key);
        
        assertEquals(0, redisService.sortedSetSize(key));
        redisService.sortedSetAdd(key, "a", 200);
        redisService.sortedSetAdd(key, "b", 100);
        redisService.sortedSetAdd(key, "b", 101);
        redisService.sortedSetAdd(key, "c", 102);
        redisService.sortedSetAdd(key, "d", 103);
        assertEquals(4, redisService.sortedSetSize(key));
        redisService.sortedSetRemove(key, "d");
        assertEquals(3, redisService.sortedSetSize(key));
        redisService.sortedSetRemoveByScore(key, 102, 104);
        assertEquals(2, redisService.sortedSetSize(key));
        assertEquals(0, redisService.sortedSetScore(key, "c"), 0.01);
        assertEquals(101, redisService.sortedSetScore(key, "b"), 0.01);
        redisService.sortedSetIncreaseScore(key, "b", 100);
        assertEquals(201, redisService.sortedSetScore(key, "b"), 0.01);
        
        List<Pair<String, Double>> list = redisService.sortedSetScan(key, 100);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("a", list.get(0).getLeft());
        
        List<Pair<Integer, Double>> list1 = redisService.sortedSetScan(key, 100, Integer.class);
        assertNotNull(list1);
        assertEquals(2, list1.size());
        assertNull(list1.get(0).getLeft());
        
        redisService.delete(key);
    }
    
    @Test
    public void setTest() {
        String key = "test_ut_set";
        redisService.delete(key);
        
        assertEquals(1, redisService.unsortedSetAdd(key, "a"));
        assertEquals(1, redisService.unsortedSetAdd(key, "b"));
        assertEquals(0, redisService.unsortedSetAdd(key, "b"));
        assertEquals(3, redisService.unsortedSetAdd(key, Arrays.asList("a", "b", "c", "d", "1")));
        
        assertEquals(5, redisService.unsortedSetSize(key));
        
        Set<String> values = redisService.unsortedSetGetAll(key);
        assertNotNull(values);
        assertEquals(5, values.size());
        assertTrue(values.contains("a"));
        assertTrue(values.contains("1"));
        
        Set<Integer> values1 = redisService.unsortedSetGetAll(key, Integer.class);
        assertNotNull(values1);
        assertEquals(1, values1.size());
        assertTrue(values1.contains(1));
        
        assertTrue(redisService.unsortedSetExists(key, "a"));
        assertTrue(redisService.unsortedSetExists(key, "d"));
        assertFalse(redisService.unsortedSetExists(key, "e"));
        
        assertEquals(0, redisService.unsortedSetRemove(key, "f"));
        assertEquals(5, redisService.unsortedSetSize(key));
        assertEquals(1, redisService.unsortedSetRemove(key, "d"));
        assertEquals(4, redisService.unsortedSetSize(key));
        assertEquals(1, redisService.unsortedSetRemove(key, 1));
        assertEquals(3, redisService.unsortedSetSize(key));
        assertEquals(2, redisService.unsortedSetRemove(key, Arrays.asList("a", "b")));
        assertEquals(1, redisService.unsortedSetSize(key));
        
        redisService.delete(key);
    }
}