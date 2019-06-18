package com.yoloho.data.cache.redis.support;

import com.alibaba.fastjson.JSON;

public class RedisUtil {

    /**
     * 转化对象为byte格式
     * @param obj
     * @return
     */
    public static byte[] getBytesFromObject(Object obj){
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj).getBytes();
    }
    /**
     * 转化byte格式为对象
     * @return
     */
    public static <T> T getObjectFromBytes(byte[] bytes,Class<T> clazz){
        if (bytes == null) {
            return null;
        }
        return JSON.parseObject(bytes,clazz);
    }
    /**
     * 对象转成json
     * @param obj
     * @return
     */
    public static String getJsonFromObject(Object obj){
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj);
    }
}
