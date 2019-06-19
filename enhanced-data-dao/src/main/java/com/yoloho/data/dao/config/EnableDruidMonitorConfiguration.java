package com.yoloho.data.dao.config;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.yoloho.data.dao.annotations.EnableDruidMonitor;
import com.yoloho.data.dao.monitor.DruidMonitorInit;

/**
 * @author jason
 *
 */
public class EnableDruidMonitorConfiguration implements DeferredImportSelector {
    public static class EnhancedConfiguration implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(EnableDruidMonitor.class.getName());
            String projectName = (String)map.get("projectName");
            String beanName = (String)map.get("beanName");
            Integer intervelInSeconds = (Integer)map.get("intervelInSeconds");
            if (StringUtils.isEmpty(projectName)) {
                throw new RuntimeException("Project name should be empty in EnableDruidMonitor");
            }
            DruidMonitorInit.init(registry, projectName, beanName, intervelInSeconds);
        }
    }
    
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {EnhancedConfiguration.class.getName()};
    }
}
