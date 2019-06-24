package com.yoloho.enhanced.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存配置，标注在类上
 * 
 * @author jason<jason@dayima.com> Jun 5, 2018
 * 
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableCacheConfig {
    /**
     * 缓存分组
     * 
     * @return
     */
    String group() default "";
    /**
     * 是否启用远程缓存(L1)(redis)
     * 
     * @return
     */
    EnableCacheBoolean remote() default EnableCacheBoolean.UNSET;
    /**
     * 是否启用本地缓存(L2)
     * <p>
     * 注意，本地缓存没有分布式一致性
     * 
     * @return
     */
    EnableCacheBoolean local() default EnableCacheBoolean.UNSET;
    /**
     * 过期时间，单位秒，不设置默认为300秒
     * 
     * @return
     */
    int expire() default 0;
    /**
     * 本地缓存过期时间，单位秒，不设置默认为与远端过期时间相同，
     * 按组，同组只能有一个设置有效
     * 覆盖顺序可能与执行顺序有关，这一点请平时使用时注意
     * 尽量不要重复设置利用覆盖来设置
     * 
     * @return
     */
    int expireLocal() default 0;
    /**
     * 本地缓存最多放置对象个数，超出则lru，
     * 按组，同组只能有一个设置有效
     * 覆盖顺序可能与执行顺序有关，这一点请平时使用时注意
     * 尽量不要重复设置利用覆盖来设置
     * 
     * @return
     */
    int maxSizeLocal() default 0;
}
