package com.yoloho.enhanced.data.cache.redis.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.alibaba.fastjson.JSON;

public class RedisUtil {
    private static final Class<?>[] primativeClass = new Class<?>[] {
        Byte.class, Character.class, Short.class, Integer.class, Long.class, Double.class, Float.class, Boolean.class
    };
    private static final Method[] primativeClassValueOf;
    static {
        primativeClassValueOf = new Method[primativeClass.length];
        for (int i = 0; i < primativeClass.length; i++) {
            try {
                Method m = primativeClass[i].getMethod("valueOf", String.class);
                primativeClassValueOf[i] = m;
            } catch (Exception e) {
                primativeClassValueOf[i] = null;
            }
        }
    }

    /**
     * object -> bytes
     * 
     * @param obj
     * @return
     */
    public static byte[] toBytes(Object obj){
        if (obj == null) {
            return null;
        }
        Class<?> clz = obj.getClass();
        if (String.class.isAssignableFrom(clz)) {
            return ((String)obj).getBytes();
        }
        for (Class<?> src : primativeClass) {
            if (src.isAssignableFrom(clz)) {
                return obj.toString().getBytes();
            }
        }
        return JSON.toJSONString(obj).getBytes();
    }
    
    /**
     * bytes -> object
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T toObject(String str, Class<T> clazz) {
        if (str == null) {
            return null;
        }
        if (String.class.isAssignableFrom(clazz)) {
            return (T)str;
        }
        for (int i = 0; i < primativeClass.length; i++) {
            if (primativeClass[i].isAssignableFrom(clazz)) {
                if (primativeClassValueOf[i] != null) {
                    try {
                        return (T)primativeClassValueOf[i].invoke(clazz, str);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    }
                }
            }
        }
        try {
            return JSON.parseObject(str, clazz);
        } catch (Exception e) {
        }
        return null;
    }
    /**
     * object -> string
     * 
     * @param obj
     * @return
     */
    public static String toString(Object obj){
        if (obj == null) {
            return null;
        }
        Class<?> clz = obj.getClass();
        if (String.class.isAssignableFrom(clz)) {
            return (String)obj;
        }
        for (Class<?> src : primativeClass) {
            if (src.isAssignableFrom(clz)) {
                return String.valueOf(obj);
            }
        }
        return JSON.toJSONString(obj);
    }
}
