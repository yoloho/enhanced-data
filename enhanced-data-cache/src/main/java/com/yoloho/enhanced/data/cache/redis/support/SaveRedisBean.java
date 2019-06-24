package com.yoloho.enhanced.data.cache.redis.support;

/**
 * 保存键值对的bean
 *
 * @author wuzl
 */
public class SaveRedisBean {
    private String key;
    private Object value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
