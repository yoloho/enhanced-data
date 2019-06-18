package com.yoloho.data.dao.impl;

import java.util.Arrays;

import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yoloho.data.dao.support.EnhancedConfig;
import com.yoloho.data.dao.support.EnhancedDaoParser;

/**
 * Direct connect to jdbc
 * 
 * @author jason
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:context-no-anno.xml", inheritLocations = false)
public class EnhancedDaoManualTest extends EnhancedDaoImplTest {
    public static class Bean implements BeanDefinitionRegistryPostProcessor {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            EnhancedConfig config = new EnhancedConfig();
            config.setScanPath(Arrays.asList("com.yoloho.data"));
            config.setSqlSessionFactory("mybatisSessionFactory");
            EnhancedDaoParser.scan(config, registry);
        }
    }

}
