package com.yoloho.data.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.data.dao.config.EnableEnhancedDaoConfiguration;
import com.yoloho.data.dao.monitor.MonitorCallback;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableEnhancedDaoConfiguration.class)
public @interface EnableDruidMonitor {
    /**
     * Name of the project to identify the monitor data.
     * 
     * @return
     */
    String projectName();
    
    /**
     * Custom {@link MonitorCallback} implementation.
     * Null for default implementation using falcon backend.
     * 
     * @return
     */
    String beanName();
    
    /**
     * Intervel of between sensing in seconds
     * 
     * @return
     */
    int intervelInSeconds() default 60;
}
