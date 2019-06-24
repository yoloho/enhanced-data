package com.yoloho.enhanced.data.dao.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom field mapped into database
 * <b>Not recommend to use</b>
 * 
 * @author jason
 * @deprecated
 *
 */
@Documented  
@Inherited  
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface FieldName {
    String value() default "";
}
