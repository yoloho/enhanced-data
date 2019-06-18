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
 * 简单写的单元测试，直接连了个测试库
 * 
 * @author jason<jason@dayima.com> @ May 29, 2018
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
            config.setScanPath(Arrays.asList("com.yoloho.mybatis.common"));
            config.setSqlSessionFactory("mybatisSessionFactory");
            EnhancedDaoParser.scan(config, registry);
        }
    }

}
