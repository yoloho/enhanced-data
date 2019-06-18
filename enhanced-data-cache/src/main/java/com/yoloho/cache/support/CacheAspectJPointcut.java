package com.yoloho.cache.support;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * AspectJ模式下的切面
 * 
 * @author jason<jason@dayima.com> @ Mar 20, 2019
 *
 */
@Aspect
public class CacheAspectJPointcut {
    private static final Logger logger = LoggerFactory.getLogger(CacheAspectJPointcut.class.getSimpleName());
    
    public CacheAspectJPointcut() {
        logger.info("init AspectJ pointcut");
    }
    
    public CacheAspectJPointcut(String ns) {
        this(ns, null);
    }
    
    public CacheAspectJPointcut(String ns, RedisTemplate<String, Object> redisTemplate) {
        if (ns == null || ns.length() == 0) {
            throw new RuntimeException("namespace for cache should not be empty");
        }
        CacheProcessor.setNamespace(ns);
        if (redisTemplate != null) {
            CacheProcessor.setRedisTemplate(redisTemplate);
        }
    }
    
    @Around("(@annotation(com.yoloho.cache.annotation.EnableCache) || @annotation(com.yoloho.cache.annotation.EnableCacheEvict)) && execution(* *(..))")
    public Object enableCache(ProceedingJoinPoint pjp) throws Throwable {
        Method methodImplemented = CacheProcessor.processMethodIfNecessaryUnderAspectJ(pjp);
        if (methodImplemented == null) {
            return pjp.proceed();
        }
        List<String> keyList = CacheProcessor.cacheEvictKeys(methodImplemented, pjp.getArgs());
        Object finalValue = CacheProcessor.cacheInvoke(methodImplemented, pjp);
        //处理缓存失效
        if (keyList != null) {
            CacheProcessor.cacheEvict(keyList, methodImplemented);
        }
        return finalValue;
    }
}
