enhanced-data-dao
===

* [DAO Implementation](#dao-implementation)
* [Design](#design)
* [Feature](#feature)
* [Quick Start](#quick-start)
	* [Model](#model)
	* [Generate Dao Bean](#generate-dao-bean)
		* [Manually by Code](#manually-by-code)
		* [XML](#xml)
		* [Annotation](#annotation)
	* [Monitor the Druid Pool](#monitor-the-druid-pool)
		* [XML](#xml)
		* [Annotation](#annotation)
		* [Scan Manually](#scan-manually)
	* [Generate SqlSessionFactory](#generate-sqlsessionfactory)

# DAO Implementation
Brief: 
  
* EnhancedDao
* `<enhanced:dao scan-path="" sql-session-factory="" postfix="" />`
* Annotation support through `EnableEnhancedDao`

# Design
The tradition when we operate the rows in a table through `mybatis` is to create a Mapper or write the customized sql in mappers. The number of file and work grows when the table or logic grows. Then a simple modification or even a renaming may become kinds of annoyed work. (Which means we must modify the related files and logic one by one). Once a file missed once the bug will be buried in. While we also will work on the mapping work like transform `user_id` (name of column) into `userId` (property of bean).

So it's better to find a way to prevent these kinds of duplicated work and focus on the meaningful things.

`EnhancedDao` is a product under this kind of experiment.

# Feature
* Single primary and union primaries support (see `UnionPrimaryKey`)
* Return the new auto increment primary when doing insert
* Various ways to do insertion: `insert` / `insert ignore` / `replace`
* Auto mapping between property and column name (also support customizing)
* No need for mapper
* Multiple creations: `Manually` / `xml` / `annotation`
* Range updating / deleting support (Don't forget to set `limit` and default to `1000`) (Also support `order by`, performance sensitive)
* Inheriting beans support (Not recommended for models)

```
For inherited model especially multiple levels inheriting like A > B > C, you should place @Enhanced on which should be mapped. Though it may generates useless beans in process. So it's not recommended.
```

# Quick Start

## Model

Here is an example of definition of model.

```java
private static class UnitTestUserLog {
    @PrimaryKey(autoIncrement = true)
    private int id;
    private int uid;
    private String memo;
    private int dateline;
    @IgnoreKey
    private String someKeyNotInDB;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    public String getMemo() {
        return memo;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
    public int getDateline() {
        return dateline;
    }
    public void setDateline(int dateline) {
        this.dateline = dateline;
    }
}
```

## Generate Dao Bean
### Manually by Code

```java
EnhancedDaoImpl<UnitTestUser, Integer> dao = new EnhancedDaoImpl<UnitTestUser, Integer>();
dao.setSqlSessionFactory(sqlSessionFactory);
dao.setTableName(UnitTestUser.class);
```

And you should set the `mapperLocations` of your mybatis factory by adding:
```
com/yoloho/enhanced/data/dao/xml/enhanced-dao-generic.xml
```

### XML
Schema:
```xml
http://www.dayima.org/schema/enhanced-dao http://www.dayima.org/schema/enhanced-dao/enhanced-dao.xsd
```

Bean:
```xml
<enhanced-dao:scan scan-path="com.xx.demo.model" sql-session-factory="mybatisSessionFactory" />
```

### Annotation
```java
@EnableEnhancedDao(
        scanPath = "com.xx.demo.model", 
        sqlSessionFactory = "mybatisSessionFactory", 
        postfix = "Dao" // Default is EnhancedDao
)
```

## Monitor the Druid Pool
If you use `druid` as the jdbc connection pool and want to monitor it we have integrated it.

Default receiver of monitor data is falcon client. You can config the target using properties file include definitions:

```
falcon.url=http://127.0.0.1:1988
```

Or to set it in property **BEFORE initializing**:
```
System.setProperty("falcon.url", "http://127.0.0.1:1988")
```

Or just add a startup parameter:
```shell
-Dfalcon.url=http://127.0.0.1:1988
```

### XML
```xml
<enhanced-dao:druid-monitor projectName="demo-project" />
```

Or if you want to customize the receiver, you could
```xml
<enhanced-dao:druid-monitor projectName="demo-project" callback="myReceiver" />
```

You can make you own implementation of `com.yoloho.enhanced.data.dao.monitor.MonitorCallback`.

### Annotation
```java
@EnableDruidMonitor(
	projectName = "demo-project"
)
```

### Scan Manually
```java
public class Bean implements BeanDefinitionRegistryPostProcessor {
   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
   }

   @Override
   public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
       EnhancedConfig config = new EnhancedConfig();
       config.setScanPath(Arrays.asList("com.demo.model"));
       config.setSqlSessionFactory("mybatisSessionFactory");
       EnhancedDaoParser.scan(config, registry);
   }
}
```

## Generate SqlSessionFactory
Create a `SqlSessionFactory` with connection pool support is a general demand and a little complex. So we make a scaffold to make it easy.

If we use XML to do this we may:

```xml
<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
	<property name="url" value="jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&amp;characterEncoding=utf-8&amp;allowMultiQueries=true" />
	<property name="username" value="test" />
	<property name="password" value="test" />

	<property name="initialSize" value="3" />
	<property name="minIdle" value="1" />
	<property name="maxActive" value="100" />

	<property name="maxWait" value="60000" />

	<property name="timeBetweenEvictionRunsMillis" value="60000" />

	<property name="minEvictableIdleTimeMillis" value="300000" />

	<property name="validationQuery" value="SELECT 'x'" />
	<property name="testWhileIdle" value="true" />
	<property name="testOnBorrow" value="false" />
	<property name="testOnReturn" value="false" />

	<property name="poolPreparedStatements" value="true" />
	<property name="maxPoolPreparedStatementPerConnectionSize" value="20" />

	<property name="filters" value="stat" />
</bean>
<bean id="mybatisSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
	<property name="dataSource" ref="dataSource" />
</bean>
```

By annotation we could use:
```java
@EnableSqlSessionFactory(
    name = "testSessionFactory", // Bean name
    connectionUrl = "jdbc:mysql://192.168.127.56:3306/test?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true", 
    username = "test",
    password = "test" 
)
```


