package com.yoloho.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.cache.config.EnableRedisTemplateConfiguration;

/**
 * 开启RedisService<br>
 * 属性均定义为String是为了支持placeholder
 * 
 * @author jason<jason@dayima.com> @ Mar 15, 2019
 *
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableRedisTemplateConfiguration.class)
public @interface EnableRedisTemplate {
    public static enum TemplateType {
        STRING_STRING, STRING_BYTES, BOTH
    }
    
    /**
     * bean名字
     * 
     * @return
     */
    String beanName() default "redisTemplate";
    
    /**
     * 当同时创建两种类型的Template的时候，byte[]值类型的bean名字
     * 
     * @return
     */
    String beanNameForByte() default "plainRedisTemplate";
    /**
     * template是否使用StringRedisTemplate(即String, String)<br>
     * 当需要序列化等字节值支持时，会用到，注意暂时这里会独立地创建一个连接池，<br>
     * 不与已存在的同一服务器地址共用<br>
     * 注意，RedisService不能使用非string值的Template
     * 
     * @return
     */
    TemplateType templateType() default TemplateType.STRING_STRING;
    /**
     * redis地址
     * 
     * @return
     */
    String host();
    /**
     * 端口
     * 
     * @return
     */
    String port() default "6379";
    /**
     * 连接池上限
     * 
     * @return
     */
    String maxTotal() default "40";
    /**
     * 连接池空余最大连接数
     * 
     * @return
     */
    String maxIdle() default "5";
    
    /**
     * 执行命令的超时
     * 
     * @return
     */
    String readTimeout() default "10000";
    
    /**
     * 连接到远端时超时时间
     * 
     * @return
     */
    String connectTimeout() default "5000";
    
    String timeBetweenEvictionRunsMillis() default "60000";
    String minEvictableIdleTimeMillis() default "120000";
    String testOnBorrow() default "true";
}
