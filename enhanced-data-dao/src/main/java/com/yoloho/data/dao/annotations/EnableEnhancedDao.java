package com.yoloho.data.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.data.dao.config.EnableEnhancedDaoConfiguration;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableEnhancedDaoConfiguration.class)
public @interface EnableEnhancedDao {
    /**
     * 设置要扫描的路径，多个路径用英文逗号分隔<br>
     * 需要依据其生成对应dao的bean封装列表，命名规则为<br>
     * XXXXEnhancedDao
     * <p>
     * example:<br>
     * com.yoloho.bean<br>
     * com.yoloho.bean.UserBean
     * 
     * @return
     * @see https://github.com/lukehutch/fast-classpath-scanner/wiki
     */
    String[] scanPath();
    /**
     * 需要使用的sqlSessionFactory(bean id)
     * 
     * @return
     */
    String sqlSessionFactory() default "sqlSessionFactory";
    
    /**
     * 动态生成bean的前缀，可选
     * 
     * @return
     */
    String prefix() default "";
    
    /**
     * 动态生成的bean后缀
     * 
     * @return
     */
    String postfix() default "EnhancedDao";
}
