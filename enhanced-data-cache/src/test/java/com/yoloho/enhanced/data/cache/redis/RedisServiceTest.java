package com.yoloho.enhanced.data.cache.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    }

    
    @Test
    public void getTest() {
        String key = "test_ut_ddddd";
        redisService.delete(key);
        assertNull(redisService.get(key));
        assertTrue(redisService.set(key, "ddd", 333));
        assertEquals("ddd", redisService.get(key));
        redisService.delete(key);
        assertNull(redisService.get(key));
    }

    @Test
    public void outQueue() {
        String queue = "test_ut_qqqqqq";
        redisService.delete(queue);
        assertEquals(0, redisService.getQueueSize(queue));
        redisService.inQueue(queue, "test");
        assertEquals(1, redisService.getQueueSize(queue));
        assertEquals("test", redisService.outQueue(queue));
        assertEquals(0, redisService.getQueueSize(queue));
        redisService.delete(queue);
    }
}