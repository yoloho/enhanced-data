package com.yoloho.enhanced.data.dao.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a model POJO class to be an enhanced dao object.
 * 
 * @author jason
 * @Alter by houlf Add sharding support
 */
@Documented  
@Inherited  
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.TYPE})
public @interface Enhanced {
    /**
     * Name of the auto generated bean
     * <p>
     * By default, it's like:<br>
     * <b>userEnhancedDao</b><br>
     * While "EnhancedDao" is the postfix setting in EnableEnhancedDao.
     * </p>
     * <p>
     * You can change it to a new one by setting this though it's better to follow general principles.
     * </p>
     */
    String name() default "";

    /**
     * The table is binded with.
     * <p>
     * If the table can not be auto mapped, as:
     * <pre>
     * * UserDetail =&gt; user_detail
     * * UserDDetail =&gt; user_d_detail
     * </pre>
     * You can spacify table name.
     * </p>
     */
    String tableName() default "";

    /**
     * Type
     */
    EnhancedType type() default EnhancedType.ENHANCED;
    
}
