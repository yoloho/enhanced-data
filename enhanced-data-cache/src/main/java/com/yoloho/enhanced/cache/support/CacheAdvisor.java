package com.yoloho.enhanced.cache.support;

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 简洁版缓存切面服务
 * 注意，只能一个实例
 * 
 * @author jason<jason@dayima.com> @ Jun 6, 2018
 *
 */
public class CacheAdvisor extends AbstractBeanFactoryPointcutAdvisor {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(CacheAdvisor.class.getSimpleName());
    private static final Advice advisor = new CacheInterceptor();
    private static final CachePointcut pointcut = new CachePointcut();
    
    public CacheAdvisor(String ns) {
        this(ns, null);
    }
    
    public CacheAdvisor(String ns, RedisTemplate<String, Object> redisTemplate) {
        if (ns == null || ns.length() == 0) {
            throw new RuntimeException("namespace for cache should not be empty");
        }
        setAdvice(advisor);
        CacheProcessor.setNamespace(ns);
        if (redisTemplate != null) {
            CacheProcessor.setRedisTemplate(redisTemplate);
        }
    }

    /**
     * Set the {@link ClassFilter} to use for this pointcut.
     * Default is {@link ClassFilter#TRUE}.
     */
    public void setClassFilter(ClassFilter classFilter) {
        pointcut.setClassFilter(classFilter);
    }
    
    @Override
    public Advice getAdvice() {
        return super.getAdvice();
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
}
