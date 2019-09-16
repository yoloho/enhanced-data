package com.yoloho.enhanced.cache.support;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.AnnotatedElementKey;

import com.yoloho.enhanced.cache.annotation.EnableCache;
import com.yoloho.enhanced.cache.annotation.EnableCacheBoolean;
import com.yoloho.enhanced.cache.annotation.EnableCacheConfig;

class CacheConfig {
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class.getSimpleName());
    private boolean local = false;
    private boolean remote = true;
    private String group = "default";
    private String key = "";
    private int expire = 300;
    private int expireLocal = 300;
    private int maxSizeLocal = 1000;
    private AnnotatedElementKey methodKey;

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public int getExpireLocal() {
        return expireLocal;
    }

    public void setExpireLocal(int expireLocal) {
        this.expireLocal = expireLocal;
    }

    public AnnotatedElementKey getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(AnnotatedElementKey methodKey) {
        this.methodKey = methodKey;
    }
    
    public int getMaxSizeLocal() {
        if (maxSizeLocal == 0) {
            return 1000;
        }
        return maxSizeLocal;
    }
    
    public void setMaxSizeLocal(int maxSizeLocal) {
        this.maxSizeLocal = maxSizeLocal;
    }
    
    public static void parse(Method method, Class<?> targetClass) {
        // 检查是否已存在指定的配置
        if (CacheProcessor.getCacheConfigMap().containsKey(method)) {
            return;
        }
        EnableCache enableCache = method.getAnnotation(EnableCache.class);
        EnableCacheConfig annotation = targetClass.getAnnotation(EnableCacheConfig.class);
        String groupName = enableCache.group();
        if (StringUtils.isEmpty(groupName) && annotation != null && StringUtils.isNotEmpty(annotation.group())) {
            groupName = annotation.group();
        }
        if (StringUtils.isEmpty(groupName)) {
            throw new RuntimeException("group name must specify");
        }
        CacheConfig cacheConfig = new CacheConfig();
        // XXX 这里未来考虑对完全重复的config进行复用，看实际情况的用量
        CacheProcessor.getCacheConfigMap().put(method, cacheConfig);
        cacheConfig.setGroup(groupName);
        cacheConfig.setMethodKey(new AnnotatedElementKey(method, targetClass));
        if (annotation != null && groupName.equals(annotation.group())) {
            // 设置继承
            if (StringUtils.isEmpty(enableCache.group()) || StringUtils.isEmpty(annotation.group())
                    || enableCache.group().equals(annotation.group())) {
                if (annotation.expire() > 0) {
                    cacheConfig.setExpire(annotation.expire());
                    cacheConfig.setExpireLocal(annotation.expire());
                }
                if (annotation.expireLocal() > 0) {
                    cacheConfig.setExpireLocal(annotation.expireLocal());
                }
                if (annotation.maxSizeLocal() > 0) {
                    cacheConfig.setMaxSizeLocal(annotation.maxSizeLocal());
                }
                if (annotation.remote() != EnableCacheBoolean.UNSET) {
                    cacheConfig.setRemote(annotation.remote() == EnableCacheBoolean.ENABLE);
                }
                if (annotation.local() != EnableCacheBoolean.UNSET) {
                    cacheConfig.setLocal(annotation.local() == EnableCacheBoolean.ENABLE);
                }
            }
        }
        if (enableCache.expire() > 0) {
            cacheConfig.setExpire(enableCache.expire());
            if (cacheConfig.getExpireLocal() == 0) {
                cacheConfig.setExpireLocal(enableCache.expire());
            }
        }
        if (enableCache.expireLocal() > 0) {
            cacheConfig.setExpireLocal(enableCache.expireLocal());
        }
        if (enableCache.maxSizeLocal() > 0) {
            cacheConfig.setMaxSizeLocal(enableCache.maxSizeLocal());
        }
        if (enableCache.key() != null && enableCache.key().length() > 0) {
            cacheConfig.setKey(enableCache.key());
        }
        if (enableCache.remote() != EnableCacheBoolean.UNSET) {
            cacheConfig.setRemote(enableCache.remote() == EnableCacheBoolean.ENABLE);
        }
        if (enableCache.local() != EnableCacheBoolean.UNSET) {
            cacheConfig.setLocal(enableCache.local() == EnableCacheBoolean.ENABLE);
        }
        if (!cacheConfig.isRemote() && !cacheConfig.isLocal()) {
            logger.warn("cache config {} disabled!!!!", cacheConfig.getGroup());
        }
        logger.info("init cache config for {}.{} @ {}", targetClass.getName(), method.getName(), cacheConfig.getGroup());
    }
}