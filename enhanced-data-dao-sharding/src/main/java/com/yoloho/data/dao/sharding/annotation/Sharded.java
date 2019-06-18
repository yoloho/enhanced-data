package com.yoloho.data.dao.sharding.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.yoloho.data.dao.sharding.strategy.ShardingStrategy;

/**
 * 标记一个分表Entity映射表的一些属性
 * @author houlf
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.TYPE})
public @interface Sharded {

    /**
     * dao的bean名称，默认Entity名称+EnhancedShardedDao
     */
    String dao() default "";

    /**
     * 基础表名
     * @return
     */
    String table() default "";
    
    /**
     * 分表策略
     * 对于custom策略，需要补充自定义handler类全路径
     * @return
     */
    ShardingStrategy strategy();
    
    /**
     * 分表策略处理器
     * @return
     */
    String handler() default "";
    
    /**
     * 指定分片数量，hash策略时生效
     * @return
     */
    int mode() default 0;
    
}