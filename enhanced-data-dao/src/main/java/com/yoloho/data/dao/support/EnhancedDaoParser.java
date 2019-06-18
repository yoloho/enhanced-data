package com.yoloho.data.dao.support;

import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.google.common.collect.Sets;
import com.yoloho.data.dao.api.Enhanced;
import com.yoloho.data.dao.api.EnhancedType;
import com.yoloho.data.dao.api.PrimaryKey;
import com.yoloho.data.dao.support.builder.BeanWrapper;
import com.yoloho.data.dao.support.builder.BuildContext;
import com.yoloho.data.dao.support.builder.DaoBuilder;
import com.yoloho.data.dao.support.builder.EnhancedDaoBuilder;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

public class EnhancedDaoParser extends AbstractSingleBeanDefinitionParser {
    final static private Logger logger = LoggerFactory.getLogger(EnhancedDaoParser.class.getSimpleName());
    final static private AtomicInteger atomicBeanId = new AtomicInteger();
    public static class EmptyBean {
    }
    
    @Override
    protected Class<?> getBeanClass(Element element) {
        return EmptyBean.class;
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
    
    /**
     * 由于需要做xml mapper的创建，所以尽量同一个数据库连接工厂只调用一次
     * 
     * @param config
     * @param registry
     */
    public static void scan(EnhancedConfig config, BeanDefinitionRegistry registry) {
        {
            // xml mapper
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(EnhancedDaoScanner.class);
            builder.setLazyInit(false);
            builder.addConstructorArgReference(config.getSqlSessionFactory());
            registry.registerBeanDefinition(EnhancedDaoScanner.class.getName() + "#" + atomicBeanId.getAndIncrement(),
                    builder.getBeanDefinition());
        }
        logger.info("init enhanced dao scanning on: {}", config.getScanPath());
        ScanResult scanResult = new FastClasspathScanner(config.getScanPath().toArray(new String[]{}))
                .ignoreFieldVisibility()
                .setAnnotationVisibility(RetentionPolicy.RUNTIME)
                .enableFieldAnnotationIndexing()
                .scan();
        /** 
         * 为了保证通过继承得来的@PrimaryKey也能生效，但又不每个类均去挖它的列祖列宗（降低性能），
         * 这里要求这种子类的类注解@Enhanced为必选，但可不放任何参数
         */
        List<String> result1 = scanResult.getNamesOfClassesWithFieldAnnotation(PrimaryKey.class);
        List<String> result2 = scanResult.getNamesOfClassesWithAnnotation(Enhanced.class);
        Set<String> entityClazzSet = Sets.newConcurrentHashSet();
        entityClazzSet.addAll(result1);
        entityClazzSet.addAll(result2);
        
        Map<EnhancedType, DaoBuilder> enhancedDaoBuilder = getDaoBuilders();
        Map<String, ClassInfo> entityMap = scanResult.getClassNameToClassInfo();
        for (String clazzName : entityClazzSet) {
        	ClassInfo entityInfo = entityMap.get(clazzName);
        	Class<?> clazz = getEntityClazz(clazzName);
        	//获取EnhancedType
        	EnhancedType enhanceType = getEnhancedType(clazz);
        	//构建Dao Bean
        	BeanWrapper daoWarapper = enhancedDaoBuilder.get(enhanceType).build(
        			new BuildContext(clazz, entityInfo, config), 
        			config.getSqlSessionFactory());
        	
        	//注册Dao Bean至Spring
        	registry.registerBeanDefinition(daoWarapper.getName(), daoWarapper.getBean());
        }
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        scan(new EnhancedConfig(element), parserContext.getRegistry());
    }
    
    private static Map<EnhancedType, DaoBuilder> getDaoBuilders(){
    	ServiceLoader<DaoBuilder> spiLoaders = ServiceLoader.load(DaoBuilder.class);
    	Map<EnhancedType, DaoBuilder> daoBuilderMap = new HashMap<>();
    	if(spiLoaders != null) {
        	Iterator<DaoBuilder> itor = spiLoaders.iterator();
        	while(itor.hasNext()) {
				DaoBuilder builder = itor.next();
        		daoBuilderMap.put(builder.getType(), builder);
        	}
    	}
    	
    	//检查是否自定义了EnhancedDaoBuilder
    	if(!daoBuilderMap.containsKey(EnhancedType.ENHANCED)) {
    		daoBuilderMap.put(EnhancedType.ENHANCED, new EnhancedDaoBuilder());
    	}
    	
    	return daoBuilderMap;
    }

    private static Class<?> getEntityClazz(String clazzName) {
    	try {
			return Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
    }
    
	private static EnhancedType getEnhancedType(Class<?> clazz) {
		Enhanced enhanced = clazz.getAnnotation(Enhanced.class);
		if (enhanced != null && enhanced.type() != null) {
			return enhanced.type();
		} else {
			return EnhancedType.ENHANCED;
		}
	}    
    
}