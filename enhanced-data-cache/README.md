# enhanced-data-cache
主要是对老的GenericService/GenericDao的缓存拦截和手动调用实现，
以及最新的EnableCache方式缓存（借助enhanced-cache:init）

老的实现的问题主要有Service与Dao权责不明确（dao功能上浮破坏封装性），定义麻烦，通用拦截缓存意义不大，
手动增加的缓存注解设置不明确且无本地的二级缓存支持，因此做了新的缓存机制尝试，可以试验效果改进程度。

# @EnableCacheConfig @EnableCache @EnableCacheEvict
新的缓存机制中，核心的注解就这两个，通过注解的属性，可设置对应的类或方法工作在以下三种模式：
![三种工作模式](../graph/cache.png)

具体的属性配置见注解的各方法注释说明

## 引入方式
通过enhanced-cache:init进行开启  
纯本地缓存的配置
```xml
<enhanced-cache:init namespace="test" />
```
带分布式的缓存配置
```xml
<enhanced-cache:init namespace="test" redis-ref="redisTemplate" />
```
其中，  
* namespace  
缓存的命名空间，这里需要每个项目有独立的命名空间，这样能在共用redis的情况下做隔离
* redis-ref  
远程缓存引用的redisTemplate对象的定义id  
注意，这里需要定义其为*RedisTemplate<String, Object>*类型  
更多类型如有需要，未来再行扩展。
* use-aspectj
Whether to use aspectj

完整示例xml：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:enhanced-cache="http://www.dayima.org/schema/enhanced-cache"
	xsi:schemaLocation="
	    http://www.springframework.org/schema/beans       http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context     http://www.springframework.org/schema/context/spring-context.xsd
		http://www.springframework.org/schema/aop        http://www.springframework.org/schema/aop/spring-aop.xsd
		http://www.dayima.org/schema/enhanced-cache      http://www.dayima.org/schema/enhanced-cache/enhanced-cache.xsd
	" default-autowire="byName">
	<enhanced-cache:init namespace="test1" redis-ref="redisTemplate" />
	<context:component-scan base-package="com.yoloho.cache" />
	<bean id="poolConfig" class="redis.clients.jedis.JedisPoolConfig">
        <property name="maxTotal" value="100"/>
        <property name="maxIdle" value="10"/>
        <property name="testOnBorrow" value="true"/>
    </bean>
    <bean id="redisConnectionFactory"
          class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
        <property name="hostName" value="192.168.123.3"/>
        <property name="port" value="6379"/>
        <property name="poolConfig" ref="poolConfig"/>
    </bean>
    <bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate">
        <property name="connectionFactory" ref="redisConnectionFactory"/>
        <property name="keySerializer">
			<bean
				class="org.springframework.data.redis.serializer.StringRedisSerializer"></bean>
		</property>
    </bean>
</beans>
```

## 缓存使用方式
### Spring AOP
默认方式，工作于proxy层，也就是只有过“Proxy”的调用才能被拦截和缓存，直接在同一个类内部的“本地调用”是不会经过拦截的，如：

```java
    @EnableCache
    public int getNewValue() {
        return 2;
    }
    
    public int compose() {
        return getNewValue();
    }
```

调用compose()时，getNewValue()的注解不生效，需要中转proxy

```java
	@Autowired
	private DemoService demoService;
	
    @EnableCache
    public int getNewValue() {
        return 2;
    }
    
    public int compose() {
        return demoService.getNewValue();
    }
```
可以嵌套

```java
    @EnableCache
    public int getNewValue() {
        return 2;
    }
    
    @EnableCache
    public int compose() {
        return demoService.getNewValue();
    }
```
通过proxy方式调用compose()时，compose的注解生效，getNewValue也会生效。

#### Spring AOP方式注意事项
* **被注解的方法不能为类似老的GenericServiceImpl重载而来的方法**

### AspectJ
当Spring的AOP无法满足需求，或是非常需要`内部调用`也能支持缓存注解，且不论是private还是public，则需要采用`AspectJ`加载方式。但该方式虽然更强大，但性能和易用性上比Spring AOP要差些。

共计需要4步，步骤如下：

#### 引入依赖
引入aspectjweaver.jar的依赖，必须大于1.6.3，在dayima-parent已有定义，默认为1.9.2

#### -javaagent
在jvm启动参数中，增加javaagent参数，指定为aspectjweaver包，例如

```shell
-javaagent:lib/aspectjweaver-1.9.2.jar
```

#### 缓存引入参数
如果是xml引入的传统项目：

```xml
<enhanced-cache:init 
	namespace="test1" 
	redis-ref="redisTemplate" 
	use-aspectj="true" />
```

如果是Spring Boot项目：

```java
@InitCache(
        namespace = "data-flow", 
        useAspectJ = true)
```

#### aop.xml(可选步骤)
默认已经会应用于以下包前缀：

* com.yoloho

当默认引入的不满足需求时（有新的包路径，例如新的项目），需要
为项目资源目录下增加META-INF/aop.xml文件，内容如下：

```xml
<!DOCTYPE aspectj PUBLIC
        "-//AspectJ//DTD//EN" "http://www.eclipse.org/aspectj/dtd/aspectj1.dtd">
<aspectj>
    <weaver>
        <!-- 这里的例子限定了被应用切面的包路径前缀，注意里面的两个点号语法，可根据实际项目设置，减少应用范围可提高性能 -->
        <include within="com.yoloho..*" />
    </weaver>
</aspectj>
```

## 其它注意事项
* 返回值必须为Serializable
* 返回值切勿包括“类中类”，否则会把所在类也给序列化进去，导致非常大，并且要求所在类也实现Serializable  
所以待缓存的类须为“简单类”或主类型

如：
```java
public class Demo {
	public static class Another implements Serializable {
	}
}
```
这样就不推荐使用

## 缓存使用示例
给出一个单元测试定义的示例：

```java
package com.yoloho.cache;

import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id = 0;
    private String name = "test";
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
```

```java
package com.yoloho.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.yoloho.cache.annotation.EnableCache;
import com.yoloho.cache.annotation.EnableCacheBoolean;
import com.yoloho.cache.annotation.EnableCacheConfig;

@Service
@EnableCacheConfig(group = "newgroup", expire = 77, local = EnableCacheBoolean.ENABLE, remote = EnableCacheBoolean.ENABLE)
public class DemoService {
    @Autowired
    private DemoService demoService;
    
    @EnableCache(group = "other", remote = EnableCacheBoolean.ENABLE, local = EnableCacheBoolean.ENABLE, expire = 60)
    public String getValue() {
        return null;
    }
    
    @EnableCache(expire = 44)
    public int getNewValue() {
        return 2;
    }
    
    @EnableCache(key = "'val_' + #n")
    public int getNewValue(int n) {
        return 2 * n;
    }
    
    @EnableCache
    public int compose() {
        return demoService.getNewValue();
    }
    
    @EnableCache
    public List<Item> array() {
        return Lists.newArrayList(new Item(), new Item(), new Item(), new Item());
    }
    
    @EnableCacheEvict(group = {"newgroup", "other"}, key = {"'val_' + #n"})
    public void update(int n) {
        //nothing
        logger.info("update: {}", n);
    }
}

```



