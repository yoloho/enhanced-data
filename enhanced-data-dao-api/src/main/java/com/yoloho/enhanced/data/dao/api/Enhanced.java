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
     * Name of the auto generated bean<br>
     * By default, it's like:<br>
     * UserEnhancedDao<br>
     * While "EnhancedDao" is the prefix setting in {@link EnableEnhancedDao}.
     * <p>
     * You can change it to a new one by setting this though it's better to follow general principles.
     */
    String name() default "";

    /**
     * The table is binded with.
     * <p>
     * If the table can not be auto mapped, eg.<br> 
     * UserDetail => user_detail<br>
     * UserDDetail => user_d_detail<br>
     * <br>
     * You can customize actual table name by this.
     */
    String tableName() default "";

    /**
     * Type
     */
    EnhancedType type() default EnhancedType.ENHANCED;
    
}
