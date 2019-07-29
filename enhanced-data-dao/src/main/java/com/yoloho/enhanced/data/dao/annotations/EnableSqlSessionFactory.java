package com.yoloho.enhanced.data.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.enhanced.data.dao.config.EnableSqlSessionFactoryConfiguration;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableSqlSessionFactoryConfiguration.class)
public @interface EnableSqlSessionFactory {
    // Note: To support placeholders all property should be string
    /**
     * Factory bean name
     * 
     * @return
     */
    String name() default "mybatisSessionFactory";
    
    /**
     * JDBC connection string
     * 
     * @return
     */
    String connectionUrl();

    String username();

    String password();

    /**
     * Initial connection count of the pool
     * 
     * @return
     */
    String initialSize() default "5";

    /**
     * Minimum connection count of the pool
     * 
     * @return
     */
    String minIdle() default "2";
    
    /**
     * Maximum connection count
     * 
     * @return
     */
    String maxActive() default "100";
    
    /**
     * Max time waiting to get a connection
     * 
     * @return
     */
    String maxWaitMillis() default "60000";
    
    /**
     * Default charset
     * 
     * @return
     */
    String charset() default "utf8mb4";
}
