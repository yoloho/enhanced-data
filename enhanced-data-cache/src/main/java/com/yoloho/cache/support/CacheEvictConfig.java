package com.yoloho.cache.support;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.context.expression.AnnotatedElementKey;

public class CacheEvictConfig {
    private List<String> groups;
    private List<String> keys;
    private AnnotatedElementKey methodKey;
    
    public CacheEvictConfig(List<String> groups, List<String> keys, Method method, Class<?> targetClass) {
        this.groups = groups;
        this.keys = keys;
        setMethodKey(method, targetClass);
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
    
    public AnnotatedElementKey getMethodKey() {
        return methodKey;
    }
    
    public void setMethodKey(Method method, Class<?> targetClass) {
        this.methodKey = new AnnotatedElementKey(method, targetClass);
    }
}
