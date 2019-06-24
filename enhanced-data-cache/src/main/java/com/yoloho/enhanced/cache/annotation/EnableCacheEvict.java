package com.yoloho.enhanced.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生效一个或多个指定的缓存
 * 
 * @author jason<jason@dayima.com> Jun 5, 2018
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableCacheEvict {
    /**
     * 指定要失效的缓存所在分组，可以继承EnableCacheConfig，支持多个指定
     * 
     * @return
     */
    String[] group() default "";
    /**
     * 待失效的缓存key
     * <p>
     * 支持el表达式，可以写多个
     * 
     * @return
     */
    String[] key() default "";
}
