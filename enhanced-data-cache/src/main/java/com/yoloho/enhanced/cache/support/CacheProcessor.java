package com.yoloho.enhanced.cache.support;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ClassUtils;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.yoloho.enhanced.cache.annotation.EnableCache;
import com.yoloho.enhanced.cache.annotation.EnableCacheConfig;
import com.yoloho.enhanced.cache.annotation.EnableCacheEvict;
import com.yoloho.enhanced.common.util.DigestUtil;

/**
 * 缓存逻辑
 * 
 * @author jason<jason@dayima.com> Mar 20, 2019
 *
 */
class CacheProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CacheProcessor.class.getSimpleName());
    private static final int NO_PARAM_KEY = 0;
    private static final int NULL_PARAM_KEY = 53;
    private static final Object nullValue = new Object();
    private static final String nullValueString = "__NULL__";
    private static final CacheExpressionParser parser = new CacheExpressionParser();
    private static final Map<String, Cache<String, Object>> localCacheManager = Maps.newConcurrentMap();
    /**
     * 缓存命名空间
     */
    private static String namespace;
    /**
     * 对面向接口编程时的实现类中的注解，做一下method缓存
     */
    private static final Map<Class<?>, Map<Method, Method>> implementatedMethodsCache = Maps.newConcurrentMap();
    private static volatile RedisTemplate<String, Object> redisTemplate = null;
    private static final Map<Method, CacheConfig> cacheConfigMap = Maps.newConcurrentMap();
    private static final Map<Method, CacheEvictConfig> cacheEvictConfigMap = Maps.newConcurrentMap();
    public static void parseConfig(Method method, Class<?> targetClass) {
        synchronized (CacheAdvisor.class) {
            CacheConfig.parse(method, targetClass);
        }
    }
    public static void noConfig(Method method, Class<?> targetClass) {
        synchronized (CacheAdvisor.class) {
            // 检查是否已存在指定的配置
            if (cacheConfigMap.containsKey(method)) {
                return;
            }
            CacheConfig cacheConfig = new CacheConfig();
            cacheConfigMap.put(method, cacheConfig);
            cacheConfig.setRemote(false);
            cacheConfig.setLocal(false);
            logger.info("init no cache config for {}.{} @ {}", targetClass.getName(), method.getName(), cacheConfig.getGroup());
        }
    }
    
    public static void parseEvictConfig(Method method, Class<?> targetClass) {
        synchronized (CacheAdvisor.class) {
            EnableCacheConfig enableCacheConfig = targetClass.getAnnotation(EnableCacheConfig.class);
            EnableCacheEvict enableCacheEvict = method.getAnnotation(EnableCacheEvict.class);
            List<String> groups = Lists.newArrayList();
            List<String> keys = Lists.newArrayList();
            if (enableCacheEvict.group() != null && enableCacheEvict.group().length > 0) {
                for (int i = 0; i < enableCacheEvict.group().length; i++) {
                    if (StringUtils.isNotEmpty(enableCacheEvict.group()[i])) {
                        groups.add(enableCacheEvict.group()[i]);
                    }
                }
            }
            if (groups.size() == 0 && enableCacheConfig != null && enableCacheConfig.group().length() > 0) {
                groups.add(enableCacheConfig.group());
            }
            Preconditions.checkArgument(groups.size() > 0, "无法决定待失效缓存所在分组: {}.{}", targetClass.getName(), method.getName());
            for (int i = 0; i < enableCacheEvict.key().length; i++) {
                keys.add(enableCacheEvict.key()[i]);
            }
            Preconditions.checkArgument(keys.size() > 0, "待失效缓存key设置为空: {}.{}", targetClass.getName(), method.getName());
            cacheEvictConfigMap.put(method, new CacheEvictConfig(groups, keys, method, targetClass));
        }
    }
    
    public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        CacheProcessor.redisTemplate = redisTemplate;
    }
    
    public static void setNamespace(String namespace) {
        CacheProcessor.namespace = namespace;
    }
    
    private static String getCacheKey(String key, String group) {
        return String.format("S:%s:%s:%s", namespace, group, key);
    }
    
    /**
     * 两种模式下合用的逻辑
     * 
     * @param cacheConfig
     * @param target
     * @param methodImplemented
     * @param args
     * @param invocation
     * @param inAspect 这里为了稍微提高一点点性能直接采用标记来避免去判断类型 
     * @return
     */
    private static Object invoke(CacheConfig cacheConfig, Object target, Method methodImplemented, Object[] args,
            Object invocation, boolean inAspect) throws Throwable {
        String key = null;
        if (cacheConfig.getKey() != null && cacheConfig.getKey().length() > 0) {
            key = (String) parser.parse(cacheConfig.getKey(), cacheConfig.getMethodKey(), methodImplemented, args);
        } else {
            key = generateKeyForMethod(target, methodImplemented, args);
        }
        String finalKey = getCacheKey(key, cacheConfig.getGroup());
        Object finalValue = null;
        Cache<String, Object> localCache = null;
        if (cacheConfig.isLocal()) {
            localCache = localCacheManager.get(cacheConfig.getGroup());
            if (localCache == null) {
                // 这里一次即可，所以先加个锁
                synchronized (localCacheManager) {
                    if (!localCacheManager.containsKey(cacheConfig.getGroup())) {
                        localCache = CacheBuilder.newBuilder()
                                .initialCapacity(cacheConfig.getMaxSizeLocal() > 100 ? 100 : cacheConfig.getMaxSizeLocal())
                                .maximumSize(cacheConfig.getMaxSizeLocal())
                                .expireAfterWrite(cacheConfig.getExpireLocal(), TimeUnit.SECONDS)
                                .<String, Object>build();
                        localCacheManager.put(cacheConfig.getGroup(), localCache);
                    } else {
                        localCache = localCacheManager.get(cacheConfig.getGroup());
                    }
                }
            }
        }
        if (localCache != null) {
            //read from local
            finalValue = localCache.getIfPresent(finalKey);
            if (finalValue != null && finalValue != nullValue) {
                //对于localCache，需要取出独立的副本防止被外部修改影响
                finalValue = SerializationUtils.clone((Serializable)finalValue);
            }
        }
        boolean fromRedis = false;
        if (cacheConfig.isRemote() && finalValue == null) {
            if (redisTemplate != null) {
                //read from remote
                try {
                    Object val = redisTemplate.opsForValue().get(finalKey);
                    if (val == null) {
                        // not exist in remote
                    } else if (val.equals(nullValueString)) {
                        finalValue = nullValue;
                        fromRedis = true;
                    } else {
                        finalValue = val;
                        fromRedis = true;
                    }
                } catch (Exception e) {
                    logger.warn("cache get failed: {}", e.getMessage());
                    //treat as not exists
                }
            } else {
                logger.warn("redis for cache is not presented");
            }
        }
        // 判断是否还需要真正invoke
        boolean invoked = false;
        if (finalValue == null) {
            invoked = true;
            if (inAspect) {
                finalValue = ((ProceedingJoinPoint)invocation).proceed();
            } else {
                finalValue = ((MethodInvocation)invocation).proceed();
            }
        }
        // 缓存后处理，判断是否需要更新
        if (localCache != null && (invoked || fromRedis)) {
            //save to local
            if (finalValue == null || finalValue == nullValue) {
                localCache.put(finalKey, nullValue);
            } else {
                localCache.put(finalKey, finalValue);
                //对于localCache，需要独立的副本进行存储防止被外部修改影响
                finalValue = SerializationUtils.clone((Serializable)finalValue);
            }
        }
        if (cacheConfig.isRemote() && invoked) {
            //save to remote
            if (redisTemplate != null) {
                if (finalValue == null) {
                    redisTemplate.opsForValue().set(finalKey, nullValueString, cacheConfig.getExpire(), TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(finalKey, finalValue, cacheConfig.getExpire(), TimeUnit.SECONDS);
                }
            }
        }
        if (finalValue == nullValue) {
            return null;
        }
        return finalValue;
    }
    
    /**
     * AspectJ下的处理
     * 
     * @param methodImplemented
     * @param pjp
     * @return
     * @throws Throwable
     */
    public static Object cacheInvoke(Method methodImplemented, ProceedingJoinPoint pjp) throws Throwable {
        CacheConfig cacheConfig = cacheConfigMap.get(methodImplemented);
        if (cacheConfig == null) {
            logger.warn("can not find cache config for {}", methodImplemented);
            return pjp.proceed();
        }
        if (!cacheConfig.isLocal() && !cacheConfig.isRemote()) {
            // no cache enabled
            return pjp.proceed();
        }
        return invoke(cacheConfig, pjp.getTarget(), methodImplemented, pjp.getArgs(), pjp, true);
    }
    
    /**
     * Proxy下的处理
     * 
     * @param methodImplemented
     * @param invocation
     * @return
     * @throws Throwable
     */
    public static Object cacheInvoke(Method methodImplemented, MethodInvocation invocation) throws Throwable {
        CacheConfig cacheConfig = cacheConfigMap.get(methodImplemented);
        if (cacheConfig == null) {
            logger.warn("can not find cache config for {}", methodImplemented);
            return invocation.proceed();
        }
        if (!cacheConfig.isLocal() && !cacheConfig.isRemote()) {
            // no cache enabled
            return invocation.proceed();
        }
        return invoke(cacheConfig, invocation.getThis(), methodImplemented, invocation.getArguments(), invocation,
                false);
    }
    
    /**
     * 提前拿key出来，防止参数发生变化，尤其是对象型参数
     * 
     * @param methodImplemented
     * @param invocation
     * @return
     */
    public static List<String> cacheEvictKeys(Method methodImplemented, Object[] args) {
        CacheEvictConfig cacheEvictConfig = cacheEvictConfigMap.get(methodImplemented);
        if (cacheEvictConfig == null) {
            return null;
        }
        List<String> keyList = Lists.newArrayList();
        for (String group : cacheEvictConfig.getGroups()) {
            for (String key : cacheEvictConfig.getKeys()) {
                key = (String) parser.parse(key, cacheEvictConfig.getMethodKey(), methodImplemented, args);
                key = getCacheKey(key, group);
                keyList.add(key);
            }
        }
        return keyList;
    }
    
    public static void cacheEvict(List<String> keyList, Method methodImplemented) throws Throwable {
        CacheEvictConfig cacheEvictConfig = cacheEvictConfigMap.get(methodImplemented);
        if (cacheEvictConfig == null) {
            return;
        }
        // XXX 这里考虑mdelete
        for (String group : cacheEvictConfig.getGroups()) {
            Cache<String, Object> localCache = localCacheManager.get(group);
            for (String key : keyList) {
                //remote
                if (redisTemplate != null) {
                    redisTemplate.delete(key);
                }
                //local
                if (localCache != null) {
                    localCache.invalidate(key);
                }
            }
        }
    }
    
    private static String generateKeyForMethod(Object target, Method method, Object[] args) {
        StringBuffer key = new StringBuffer();  
        key.append(target.getClass().getName()).append(".").append(method.getName()).append(":");  
        if (args == null || args.length == 0) {
            return DigestUtil.md5(key.append(NO_PARAM_KEY).toString());  
        }  
        for (Object param : args) {  
            if (param == null) {  
                logger.warn("input null param for Spring cache, use default key={}", NULL_PARAM_KEY);  
                key.append(NULL_PARAM_KEY);  
            } else if (ClassUtils.isPrimitiveArray(param.getClass())) {  
                int length = Array.getLength(param);  
                for (int i = 0; i < length; i++) {  
                    key.append(Array.get(param, i));  
                    key.append(',');  
                }
            } else if (ClassUtils.isPrimitiveOrWrapper(param.getClass()) || param instanceof String) {  
                key.append(param);  
            } else {  
                logger.warn("Using an object as a cache key may lead to unexpected results. " +  
                        "Either use @Cacheable(key=..) or implement CacheKey. Method is " + target.getClass() + "#" + method.getName());  
                key.append(param.hashCode());  
            }  
            key.append('-');  
        }  
  
        String finalKey = key.toString();  
        long cacheKeyHash = Hashing.murmur3_128().hashString(finalKey, Charset.defaultCharset()).asLong();  
        logger.debug("using cache key={} hashCode={}", finalKey, cacheKeyHash);  
        return DigestUtil.md5(key.toString());  
    }
    
    private static Map<Class<?>, Map<Method, Method>> getImplementatedMethodsCache() {
        return implementatedMethodsCache;
    }
    
    private static Method getImplementedMethodFromCache(Method method, Class<?> targetClass) {
        Map<Method, Method> methodMap = getImplementatedMethodsCache().get(targetClass);
        if (methodMap == null) {
            return null;
        }
        if (methodMap.containsKey(method)) {
            return methodMap.get(method);
        }
        return null;
    }
    
    private static void setImplementedMethodToCache(Method method, Method methodImplemented, Class<?> targetClass) {
        Map<Method, Method> methodMap = CacheProcessor.getImplementatedMethodsCache().get(targetClass);
        if (methodMap == null) {
            methodMap = Maps.newConcurrentMap();
            CacheProcessor.getImplementatedMethodsCache().put(targetClass, methodMap);
        }
        methodMap.put(method, methodImplemented);
    }
    
    /**
     * proxy模式下，获取已解析了的缓存方法
     * 
     * @param invocation
     * @return
     */
    public static Method getMethodUnderProxy(MethodInvocation invocation) {
        return getImplementedMethodFromCache(invocation.getMethod(), invocation.getThis().getClass());
    }
    
    /**
     * AspectJ模式下，返回实际方法，如果为null，则出错<br>
     * 首次调用则做解析
     * 
     * @param pjp
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method processMethodIfNecessaryUnderAspectJ(ProceedingJoinPoint pjp) throws NoSuchMethodException, SecurityException {
        Signature sig = pjp.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("缓存方法错误，拦截到了非方法调用？");
        }
        msig = (MethodSignature) sig;
        Class<?> targetClass = pjp.getTarget().getClass();
        Method method = msig.getMethod();
        Method newmethod = CacheProcessor.getImplementedMethodFromCache(method, targetClass);
        if (newmethod == null) {
            newmethod = targetClass.getDeclaredMethod(msig.getName(), msig.getParameterTypes());
            if (newmethod.isAnnotationPresent(EnableCache.class)) {
                //开启缓存标记
                CacheProcessor.parseConfig(newmethod, targetClass);
            } else {
                CacheProcessor.noConfig(newmethod, targetClass);
            }
            if (newmethod.isAnnotationPresent(EnableCacheEvict.class)) {
                //失效缓存
                CacheProcessor.parseEvictConfig(newmethod, targetClass);
            }
            CacheProcessor.setImplementedMethodToCache(method, newmethod, targetClass);
        }
        return newmethod;
    }
    
    /**
     * proxy模式下，处理方法是否有匹配注解
     * 
     * @param method
     * @param targetClass
     * @return
     */
    public static boolean processMethodAnnotation(Method method, Class<?> targetClass) {
        boolean matched = false;
        Method newmethod = null;
        if (targetClass.getInterfaces() != null && targetClass.getInterfaces().length > 0) {
            //这里对于那些实现了接口的实现类来说，需要重新拿一下方法
            try {
                newmethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException | SecurityException e) {
                newmethod = method;
            }
        } else {
            newmethod = method;
        }
        if (newmethod.isAnnotationPresent(EnableCacheEvict.class)) {
            //失效缓存
            CacheProcessor.parseEvictConfig(newmethod, targetClass);
            matched = true;
        }
        if (newmethod.isAnnotationPresent(EnableCache.class)) {
            //开启缓存标记
            CacheProcessor.parseConfig(newmethod, targetClass);
            matched = true;
        } else if (matched) {
            // 有evict无enablecache的情况
            CacheProcessor.noConfig(newmethod, targetClass);
        }
        if (matched) {
            CacheProcessor.setImplementedMethodToCache(method, newmethod, targetClass);
        }
        return matched;
    }
    
    protected static Map<Method, CacheConfig> getCacheConfigMap() {
        return cacheConfigMap;
    }
}
