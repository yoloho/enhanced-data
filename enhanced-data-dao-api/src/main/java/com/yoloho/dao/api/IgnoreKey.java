package com.yoloho.dao.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个bean属性为dao转换时忽略
 * 
 * @author jason
 *
 */
@Documented  
@Inherited  
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface IgnoreKey {
}
