package com.yoloho.enhanced.data.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.enhanced.data.dao.api.PrimaryKey;
import com.yoloho.enhanced.data.dao.config.EnableEnhancedDaoConfiguration;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableEnhancedDaoConfiguration.class)
public @interface EnableEnhancedDao {
    /**
     * The packages separated by comma to be scanned for models containing {@link PrimaryKey}.<br>
     * The generated dao bean is named by the rule:<br>
     * XXXX => XXXXEnhancedDao
     * 
     * @return
     * @see https://github.com/lukehutch/fast-classpath-scanner/wiki
     */
    String[] scanPath();
    /**
     * Referenced sqlSessionFactory(bean id)
     * 
     * @return
     */
    String sqlSessionFactory() default "sqlSessionFactory";
    
    /**
     * Generated bean's prefix.<br>
     * Default to be empty.
     * 
     * @return
     */
    String prefix() default "";
    
    /**
     * Generated bean's postfix.<br>
     * Default to "EnhancedDao"
     * 
     * @return
     */
    String postfix() default "EnhancedDao";
}
