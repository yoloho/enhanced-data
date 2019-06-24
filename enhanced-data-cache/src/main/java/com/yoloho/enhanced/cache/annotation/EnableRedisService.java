package com.yoloho.enhanced.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.enhanced.cache.config.EnableRedisServiceConfiguration;

/**
 * 开启RedisService
 * 
 * @author jason<jason@dayima.com> @ Mar 15, 2019
 *
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableRedisServiceConfiguration.class)
public @interface EnableRedisService {
    /**
     * bean名字
     * 
     * @return
     */
    String beanName() default "redisService";
    /**
     * 引用的templateBean
     * 
     * @return
     */
    String templateBeanName() default "redisTemplate";
    
    /**
     * 用于队列，如果要连不同的连接，可指定这个（这个操作不建议使用）
     * 
     * @return
     */
    String queueTemplateBeanName() default "";
}
