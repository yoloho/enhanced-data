package com.yoloho.cache.config;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.yoloho.cache.annotation.EnableRedisTemplate;
import com.yoloho.cache.annotation.EnableRedisTemplate.TemplateType;

import redis.clients.jedis.JedisPoolConfig;

/**
 * 注解引入方式的配置类
 * 
 * @author jason<jason@dayima.com> Mar 19, 2019
 *
 */
public class EnableRedisTemplateConfiguration implements DeferredImportSelector {
    /**
     * 新版的配置方式，目前暂不实现ssl
     * 
     * @author jason<jason@dayima.com> @ Mar 19, 2019
     *
     */
    public static class ClientConfiguration implements JedisClientConfiguration {
        private GenericObjectPoolConfig poolConfig;
        private Duration connectTimeout;
        private Duration readTimeout;
        public ClientConfiguration(GenericObjectPoolConfig poolConfig, long connectTimeout, long readTimeout) {
            this.poolConfig = poolConfig;
            this.connectTimeout = Duration.ofMillis(connectTimeout);
            this.readTimeout = Duration.ofMillis(readTimeout);
        }

        @Override
        public boolean isUseSsl() {
            return false;
        }

        @Override
        public Optional<SSLSocketFactory> getSslSocketFactory() {
            return Optional.ofNullable(null);
        }

        @Override
        public Optional<SSLParameters> getSslParameters() {
            return Optional.ofNullable(null);
        }

        @Override
        public Optional<HostnameVerifier> getHostnameVerifier() {
            return Optional.ofNullable(null);
        }

        @Override
        public boolean isUsePooling() {
            return true;
        }

        @Override
        public Optional<GenericObjectPoolConfig> getPoolConfig() {
            return Optional.of(poolConfig);
        }

        @Override
        public Optional<String> getClientName() {
            return Optional.ofNullable(null);
        }

        @Override
        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Duration getReadTimeout() {
            return readTimeout;
        }
        
    }
    /**
     * 拿来插入bean的中转类<br>
     * 这里之所以插入了数个bean定义，是因为需要支持placeholder
     * 
     * @author jason<jason@dayima.com> @ Mar 19, 2019
     *
     */
    public static class Configuration implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            // 取配置
            Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(EnableRedisTemplate.class.getName());
            String beanName = (String)map.get("beanName");
            String beanNameForByte = (String)map.get("beanNameForByte");
            String host = (String)map.get("host");
            String port = (String)map.get("port");
            String maxTotal = (String)map.get("maxTotal");
            String maxIdle = (String)map.get("maxIdle");
            String connectTimeout = (String)map.get("connectTimeout");
            String readTimeout = (String)map.get("readTimeout");
            String timeBetweenEvictionRunsMillis = (String)map.get("timeBetweenEvictionRunsMillis");
            String minEvictableIdleTimeMillis = (String)map.get("minEvictableIdleTimeMillis");
            String testOnBorrow = (String)map.get("testOnBorrow");
            TemplateType templateType = (TemplateType)map.get("templateType");
            
            String poolBeanName = JedisPoolConfig.class.getName() + "#" + beanName;
            String clientConfigurationBeanName = ClientConfiguration.class.getName() + "#" + beanName;
            String redisStandaloneConfigurationBeanName = RedisStandaloneConfiguration.class.getName() + "#" + beanName;
            String factoryBeanName = JedisConnectionFactory.class.getName() + "#" + beanName;
            {
                // 初始化连接池配置
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JedisPoolConfig.class);
                builder.addPropertyValue("maxTotal", maxTotal);
                builder.addPropertyValue("maxIdle", maxIdle);
                builder.addPropertyValue("timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
                builder.addPropertyValue("minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);
                builder.addPropertyValue("testOnBorrow", testOnBorrow);
                builder.setLazyInit(false);
                registry.registerBeanDefinition(poolBeanName, builder.getBeanDefinition());
            }
            {
                // ClientConfiguration
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ClientConfiguration.class);
                builder.addConstructorArgReference(poolBeanName);
                builder.addConstructorArgValue(connectTimeout);
                builder.addConstructorArgValue(readTimeout);
                builder.setLazyInit(false);
                registry.registerBeanDefinition(clientConfigurationBeanName, builder.getBeanDefinition());
            }
            {
                // RedisStandaloneConfiguration
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisStandaloneConfiguration.class);
                builder.addConstructorArgValue(host);
                builder.addConstructorArgValue(port);
                builder.setLazyInit(false);
                registry.registerBeanDefinition(redisStandaloneConfigurationBeanName, builder.getBeanDefinition());
            }
            {
                // 连接工厂
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JedisConnectionFactory.class);
                builder.addConstructorArgReference(redisStandaloneConfigurationBeanName);
                builder.addConstructorArgReference(clientConfigurationBeanName);
                builder.setLazyInit(false);
                registry.registerBeanDefinition(factoryBeanName, builder.getBeanDefinition());
            }
            {
                // 插入redisTemplate
                if (templateType == TemplateType.BOTH || templateType == TemplateType.STRING_STRING) {
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(StringRedisTemplate.class);
                    builder.addConstructorArgReference(factoryBeanName);
                    builder.setLazyInit(false);
                    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
                }
                if (templateType == TemplateType.BOTH || templateType == TemplateType.STRING_BYTES) {
                    String name = beanName;
                    if (templateType == TemplateType.BOTH) {
                        name = beanNameForByte;
                    }
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisTemplate.class);
                    builder.addPropertyValue("keySerializer", RedisSerializer.string());
                    builder.addPropertyReference("connectionFactory", factoryBeanName);
                    builder.setLazyInit(false);
                    registry.registerBeanDefinition(name, builder.getBeanDefinition());
                }
            }
        }
    }
    
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {Configuration.class.getName()};
    }
}
