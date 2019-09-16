package com.yoloho.enhanced.data.cache.xml;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.yoloho.enhanced.cache.annotation.InitCache;

/**
 * Support annotation parsing
 * 
 * @author jason
 *
 */
public class InitCacheConfiguration implements DeferredImportSelector {
    private static final Logger logger = LoggerFactory.getLogger(InitCacheConfiguration.class.getSimpleName());
    
    public static class DefaultsConfiguration implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(InitCache.class.getName());
            if (map == null) {
                logger.warn("no annotation configuration found, using defaults");
                return;
            }
            String namespace = (String)map.get("namespace");
            String redisRef = (String)map.get("redisRef");
            boolean useAspectJ = (Boolean)map.get("useAspectJ");
            InitCacheParser.initBeans(namespace, useAspectJ, redisRef, registry);
        }
    }
    
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {DefaultsConfiguration.class.getName()};
    }
}
