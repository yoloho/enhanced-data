package com.yoloho.dao.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个bean属性为primary
 * 
 * @author jason
 *
 */
@Documented  
@Inherited  
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface PrimaryKey {
    /**
     * 是否自增
     */
    boolean autoIncrement() default false;
}
