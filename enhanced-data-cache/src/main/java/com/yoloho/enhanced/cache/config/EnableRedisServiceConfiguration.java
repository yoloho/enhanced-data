package com.yoloho.enhanced.cache.config;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.yoloho.enhanced.cache.annotation.EnableRedisService;
import com.yoloho.enhanced.data.cache.redis.RedisServiceImpl;

/**
 * 注解引入方式的配置类
 * 
 * @author jason<jason@dayima.com> Mar 19, 2019
 *
 */
public class EnableRedisServiceConfiguration implements DeferredImportSelector {
    public static class Configuration implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            // 取配置
            Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(EnableRedisService.class.getName());
            String beanName = (String)map.get("beanName");
            String templateBeanName = (String)map.get("templateBeanName");
            String queueTemplateBeanName = (String)map.get("queueTemplateBeanName");
            
            // 连接工厂
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisServiceImpl.class);
            builder.addPropertyReference("redisTemplate", templateBeanName);
            if (StringUtils.isNotEmpty(queueTemplateBeanName)) {
                builder.addPropertyReference("redisQueueTemplate", queueTemplateBeanName);
            }
            builder.setLazyInit(false);
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        }
    }
    
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {Configuration.class.getName()};
    }
}
