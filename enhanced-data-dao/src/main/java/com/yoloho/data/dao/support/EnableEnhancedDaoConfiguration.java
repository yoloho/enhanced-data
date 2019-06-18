package com.yoloho.data.dao.support;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author jason
 *
 */
public class EnableEnhancedDaoConfiguration implements DeferredImportSelector {
    public static class EnhancedConfiguration implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            EnhancedDaoParser.scan(new EnhancedConfig(importingClassMetadata), registry);
        }
    }
    
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {EnhancedConfiguration.class.getName()};
    }
}
