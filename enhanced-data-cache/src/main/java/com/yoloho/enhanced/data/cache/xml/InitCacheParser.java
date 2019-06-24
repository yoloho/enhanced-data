package com.yoloho.enhanced.data.cache.xml;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class InitCacheParser extends AbstractSimpleBeanDefinitionParser {
    private final static Logger logger = LoggerFactory.getLogger(InitCacheParser.class.getSimpleName());
    protected final static String LOG_CONSOLE = "log.console";
    protected final static String LOG_FILE = "log.file";
    
    public InitCacheParser() {
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
    
    @Override
    protected Class<?> getBeanClass(Element element) {
        return InitCacheParser.class;
    }
    
    /**
     * init the bean to be injected
     * 
     * @param registry
     */
    protected static void initBeans(String namespace, boolean useAspectJ, String redisRef, BeanDefinitionRegistry registry) {
        // cache dealing
        if (StringUtils.isEmpty(namespace)) {
            throw new RuntimeException("Cache namespace can not be empty.");
        }
        AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
        String className = "com.yoloho.enhanced.cache.support.CacheAdvisor";
        if (useAspectJ) {
            // Using AspectJ
            className = "com.yoloho.enhanced.cache.support.CacheSimpleInit";
            String aspectAnnoName = "org.aspectj.lang.annotation.Aspect";
            try {
                Class.forName(aspectAnnoName);
            } catch (Exception e) {
                logger.error("init cache failed, maybe no aspectweaver.jar found");
            }
            logger.info("cache will use AspectJ");
        } else {
            // Work on proxy layer
            logger.info("cache will work on proxy layer");
        }
        try {
            Class<?> clz = Class.forName(className);
            BeanDefinitionBuilder advisorBuilder = BeanDefinitionBuilder.genericBeanDefinition(clz);
            advisorBuilder.addConstructorArgValue(namespace);
            if (StringUtils.isNotEmpty(redisRef)) {
                advisorBuilder.addConstructorArgReference(redisRef);
            }
            advisorBuilder.setLazyInit(false);
            advisorBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(className, advisorBuilder.getBeanDefinition());
        } catch (Exception e) {
            logger.warn("init caching failed", e);
        }
        logger.info("init caching support");
    }
    
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.setLazyInit(false);
        String namespace = element.getAttribute("namespace");
        String redisRef = element.getAttribute("redis-ref");
        boolean useAspectJ = StringUtils.equalsIgnoreCase(element.getAttribute("use-aspectj"), "true");
        initBeans(namespace, useAspectJ, redisRef, parserContext.getRegistry());
    }

}
