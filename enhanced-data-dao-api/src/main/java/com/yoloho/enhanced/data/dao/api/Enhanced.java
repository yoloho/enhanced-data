package com.yoloho.enhanced.data.dao.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个bean映射表的一些属性，目前主要是非规范的表名字
 * @author jason
 * @Alter by houlf 添加增强类型
 */
@Documented  
@Inherited  
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.TYPE})
public @interface Enhanced {
    /**
     * dao名字
     */
    String name() default "";

    /**
     * 表名字
     */
    String tableName() default "";

    /**
     * 增强类型
     */
    EnhancedType type() default EnhancedType.ENHANCED;
    
}
