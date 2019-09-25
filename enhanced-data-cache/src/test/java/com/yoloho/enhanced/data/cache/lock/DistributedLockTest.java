package com.yoloho.enhanced.data.cache.lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mockito.Mockito;

import com.yoloho.enhanced.data.cache.redis.api.RedisService;

public class DistributedLockTest {
    @Test
    public void lockTest() {
        RedisService redisService = Mockito.mock(RedisService.class);
        DistributedLock<Long> lock = new DistributedLock<>(redisService, "test1", 10);
        when(redisService.exists(lock.lockKey(1L))).thenReturn(false);
        when(redisService.setIfAbsent(eq(lock.lockKey(1L)), anyString(), anyInt())).thenReturn(true);
        assertTrue(lock.tryToLock(1L));
        when(redisService.setIfAbsent(eq(lock.lockKey(1L)), anyString(), anyInt())).thenReturn(false);
        assertFalse(lock.tryToLock(1L));
        when(redisService.exists(lock.lockKey(1L))).thenReturn(true);
        when(redisService.get(eq(lock.lockKey(1L)))).thenReturn("test|" + System.currentTimeMillis());
        when(redisService.setIfAbsent(eq(lock.lockKey(1L)), anyString(), anyInt())).thenReturn(true);
        assertFalse(lock.tryToLock(1L));
        try {
            lock.lock(1L, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            assertTrue(true);
        }
    }
    
    @Test
    public void customLockTest() {
        AtomicInteger atomicInteger = new AtomicInteger();     
        DistributedLock<Long> lock = new DistributedLock<>(new DistributedLock.LockSupport() {
            private ConcurrentMap<String, String> data = new ConcurrentHashMap<>();
            
            @Override
            public boolean setIfAbsent(String key, String value, int expireInSeconds) {
                return data.putIfAbsent(key, value) == null;
            }
            
            @Override
            public void keep(String key, String value, int keepInSeconds) {
                data.put(key, value);
                atomicInteger.incrementAndGet();
            }
            
            @Override
            public String get(String key) {
                return data.get(key);
            }
            
            @Override
            public boolean exists(String key) {
                return data.containsKey(key);
            }
            
            @Override
            public void delete(String key) {
                data.remove(key);
            }
        }, "test1", 10);
        assertTrue(lock.tryToLock(1L));
        assertFalse(lock.tryToLock(1L));
        assertTrue(lock.tryToLock(2L));
        lock.keepLock(2L);
        lock.keepLock(2L);
        assertEquals(2, atomicInteger.get());
        assertTrue(lock.unlock(2L));
        try {
            lock.lock(1L, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            assertTrue(true);
        }
        assertTrue(lock.unlock(1L));
        assertTrue(lock.tryToLock(1L));
        assertTrue(lock.unlock(1L));
        
        try (AutoCloseable obj = lock.lock(2L, 2, TimeUnit.SECONDS)) {
        } catch (Exception e) {
            assertTrue(false);
        }
        try (AutoCloseable obj = lock.lock(2L, 2, TimeUnit.SECONDS)) {
        } catch (Exception e) {
            assertTrue(false);
        }
    }
}
