package com.yoloho.enhanced.data.dao.config;

import java.util.Arrays;
import java.util.Map;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.yoloho.enhanced.data.dao.annotations.EnableSqlSessionFactory;

/**
 * @author jason
 *
 */
public class EnableSqlSessionFactoryConfiguration implements DeferredImportSelector {
    public static class EnhancedConfiguration implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(EnableSqlSessionFactory.class.getName());
            String name = (String)map.get("name");
            String connectionUrl = (String)map.get("connectionUrl");
            String username = (String)map.get("username");
            String password = (String)map.get("password");
            String initialSize = (String)map.get("initialSize");
            String minIdle = (String)map.get("minIdle");
            String maxActive = (String)map.get("maxActive");
            String maxWaitMillis = (String)map.get("maxWaitMillis");
            String charset = (String)map.get("charset");
            
            // DataSource
            {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
                builder.addPropertyValue("url", connectionUrl);
                builder.addPropertyValue("username", username);
                builder.addPropertyValue("password", password);
                builder.addPropertyValue("initialSize", initialSize);
                builder.addPropertyValue("minIdle", minIdle);
                builder.addPropertyValue("maxActive", maxActive);
                builder.addPropertyValue("maxWait", maxWaitMillis);
                builder.addPropertyValue("timeBetweenEvictionRunsMillis", 60000);
                builder.addPropertyValue("minEvictableIdleTimeMillis", 300000);
                builder.addPropertyValue("validationQuery", "select 'x'");
                builder.addPropertyValue("testWhileIdle", true);
                builder.addPropertyValue("testOnBorrow", false);
                builder.addPropertyValue("testOnReturn", false);
                builder.addPropertyValue("keepAlive", true);
                builder.addPropertyValue("poolPreparedStatements", false);
                builder.addPropertyValue("filters", "stat");
                builder.addPropertyValue("connectionInitSqls", Arrays.asList("set names " + charset));
                
                builder.setInitMethodName("init");
                builder.setDestroyMethodName("close");
                registry.registerBeanDefinition(name + "DataSource", builder.getBeanDefinition());
            }
            
            // SessionFactory
            {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);
                builder.addPropertyReference("dataSource", name + "DataSource");
                registry.registerBeanDefinition(name, builder.getBeanDefinition());
            }
            
            // TransactionManager
            {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
                builder.addConstructorArgReference(name + "DataSource");
                registry.registerBeanDefinition(name + "TransactionManager", builder.getBeanDefinition());
            }
        }
    }
    
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {EnhancedConfiguration.class.getName()};
    }
}
