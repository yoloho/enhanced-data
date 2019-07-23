package com.yoloho.enhanced.data.cache.redis.support;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class RedisUtilTest {
    @SuppressWarnings("unused")
    private static class Bean {
        long id = 23;
        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
    }
    
    @Test
    public void toStringTest() {
        assertNull(RedisUtil.toString(null));
        assertEquals("", RedisUtil.toString(""));
        assertEquals("a", RedisUtil.toString("a"));
        assertEquals("1", RedisUtil.toString(1));
        assertEquals("1", RedisUtil.toString(1L));
        assertEquals("1.0", RedisUtil.toString(1.0));
        assertEquals("1.0", RedisUtil.toString(1.0f));
        assertEquals("true", RedisUtil.toString(true));
        assertEquals("{\"id\":23}", RedisUtil.toString(new Bean()));
    }
    
    @Test
    public void toObjectTest() {
        assertNull(RedisUtil.toObject(null, Integer.class));
        assertEquals(new Integer(33), RedisUtil.toObject("33", Integer.class));
        assertEquals(new Long(33), RedisUtil.toObject("33", Long.class));
        assertEquals(new Double(33), RedisUtil.toObject("33", Double.class));
        assertEquals(new Float(33), RedisUtil.toObject("33", Float.class));
        assertEquals("33", RedisUtil.toObject("33", String.class));
        Bean obj = RedisUtil.toObject("{\"id\":23}", Bean.class);
        assertNotNull(obj);
        assertEquals(23, obj.getId());
    }
    
    @Test
    public void toBytesTest() {
        assertArrayEquals(null, RedisUtil.toBytes(null));
        assertArrayEquals(new byte[] {}, RedisUtil.toBytes(""));
        assertArrayEquals(new byte[] {'1'}, RedisUtil.toBytes("1"));
        assertArrayEquals(new byte[] {'1'}, RedisUtil.toBytes(1));
        assertArrayEquals(new byte[] {'2'}, RedisUtil.toBytes(2L));
        assertArrayEquals(new byte[] {'1', '.', '0'}, RedisUtil.toBytes(1.0));
    }
}
