# enhanced-data-dao-api
本项目主要封装了dao层访问的常见操作，一部分是传统的GenericDao/GenericService封装组合，另外一部分是EnhancedDao封装。  
还包括了与之配合使用的动态查询工具类DynamicQueryFilter，其它查询类已过时（因为功能性或是封装性等原因）。

# Change log

## 1.0.0
* Remove all the deprecated designs
* Prepared to open source

## 0.0.12
* 将redisService的实现类等与api定义无关的实现迁移至spring-common
* 支持spring-boot注解调用`@InitDefaults`(spring-common)
* EnhancedDao支持spring-boot，注解引用方式`@EnableEnhancedDao`

## 0.0.11
* 增加DistributedLock分布式锁实现(redis)

## tips
### tinyint变boolean问题
这是jdbc的特性，在自动映射时，当bean定义不是boolean时会提示出错，解决方法是在连接串中增加：

```
tinyInt1isBit=false
```

# DynamaicQueryFilter
基础的查询工具类，提供了新的链式调用语法，即可如下使用
```java
DynamicQueryFilter filter = new DynamicQueryFilter();
filter.equalPair("uid", 3)
	.equalPair("type", 3)
	.addFilter("dateline", Operator.lessThan, 123443333)
	.in("subType", Lists.newArrayList(1, 2, 3))
	.addFilter("name", Operator.like, Type.String, "demo")
	.addFilter("email", Operator.endsWith, Type.String, "@163.com")
	.orderBy("id", true)
	.orderBy("name", false)
	.limit(100);
```

## 条件相关

### public <T> DynamicQueryFilter equalPair(String fieldName, T value)
提供常见类型的"="操作  
同样的定义还有：
* notEqual  
* greatThan
* greatOrEqual
* lessThan
* lessOrEqual
* in
* notIn
* like
* startsWith
* endsWith

### addFilter(String fieldName, Operator operator, value)
提供常见类型的带操作符支持的设置

### addFilter(String fieldName, Operator operator, Type type, Object value)
完整设置条件的函数

### and()/or()/setLogicAnd(boolean val)
是否以"and"连接各条件，其中，推荐使用and()/or()更明确

### addSubFilter(DynamicQueryFilter filter)
子查询时使用（带层级组合条件）

### addToFilter(DynamicQueryFilter filter)
跟addSubFilter父子关系相反

### getQueryData()
提供传递给dao的对象


## 排序相关

### orderBy(String orderBy, boolean isDesc)
设置排序，注意，这里是有先后顺序的，能按多列来排序

## 分页相关

### limit(int count)
设置最多返回的行数

### limit(int offset, int count)
limit m, n

### page(int page, int pageSize)
根据逻辑上的页面与页面大小进行分页计算

## 使用举例
```java
long curTime = Long.valueOf(MeiyueUtil.curTime("yyyyMMddHHmmss"));
DynamicQueryFilter filter = new DynamicQueryFilter();
filter.equalPair("status", BaseStatusEnum.VALID.getVal())
    .lessOrEqual("startTime", curTime)
    .greatOrEqual("endTime", curTime)
    .orderBy("showOrder", true)
    .orderBy("regionalId", true)
    .page(1, 20);
List<MallRegionalService> list = mallRegionalServiceDao.find(filter.getQueryData());
```

# EnhancedDao
## Memo
It has been moved to `enhanced-data-dao` which is making more sense.

## 设计理念
由于现行的组件设计在实际大半的简单场景中，新建和修改一个数据对象时非常麻烦，往往意味着多个文件一起修改，
且存在着改漏、改错、以及明明是按既定规则的名字映射（驼峰<->下划线）但因人工编辑产生疏漏，进而产生bug，
所以期望设计一套尽量规避重复性规律性工作的人工介入程度，且轻量、在现行架构中可并行使用的渐近式的新工具，
EnhancedDao即为这样的尝试。

## 支持功能

* 单主键、联合主键（联合主键泛型需指定为UnionPrimaryKey）
* 自动增量类型字段的返回
* insert/insert ignore/replace
* 字段与bean属性自动映射
* 大部分情况下无需书写独立的mapper
* 提供了多种创建dao bean的方式（程序手动创建、xml方式、自定义标签spring-common:dao方式）
* 尝试性地提供了范围更新，但需要配合limit使用（默认最多1000行），并且delete/update（按条件）均支持order（但要按需使用，影响性能）
* 尝试性地对继承做了支持
```
为了保证通过继承得来的@PrimaryKey也能生效，但又不每个类均去挖它的列祖列宗（降低性能）  
这里要求这种子类的类注解@Enhanced为必选，但可不放任何参数  
典型示例为forumapi中的TopicEx < Topic < TopicBase  
但这种情况下，所有继承来的子类和本类均会注册bean，却并不是所有的bean均开箱即用（没实际意义，因为子类往往补充的是数据库不存在的字段）
```

## bean定义
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

## 使用方式

### 代码初始化
```java
EnhancedDaoImpl<UnitTestUser, Integer> dao = new EnhancedDaoImpl<UnitTestUser, Integer>();
dao.setSqlSessionFactory(sqlSessionFactory);
dao.setTableName(UnitTestUser.class);
```
*注意，在使用除xml扫描初始化以外的方式初始化时，mybatis定义时需要修改mapperLocations*，加入：
```
com/yoloho/enhanced/data/dao/xml/enhanced-dao-generic.xml
```

### xml初始化
xml方式主要分为bean方式和扫描式。  
bean方式与代码初始化类似，并且可选择是采用构造方法式还是创建后property设置式，不再详述。  
这里举例说明扫描式，具体的demo可见mybatis-common中的单元测试。  
```xml
<mybatis-common:enhanced-dao scan-path="com.yoloho.mybatis.common" sql-session-factory="mybatisSessionFactory" />
```
描述方式可对指定的路径下的bean对象进行扫描，多路径可逗号分隔，或多次引用（但一般不这么干）。  
默认为Bean的驼峰名字加EnhancedDao后缀，可通过注解自定义(@Enhanced)，或直接增加属性postfix="Dao"等
```xml
<mybatis-common:enhanced-dao scan-path="com.yoloho.mybatis.common" sql-session-factory="mybatisSessionFactory" postfix="Dao" />
```

### @Bean注解方式初始化
此项不再详述。

## SpringBoot配置注解支持
### @InitDefaults
### @EnableEnhancedDao

## 引入的注解

### @PrimaryKey
用于标识一个field为主键之一，并可标记为是否自增。

### @IgnoreKey
用于标记某bean属性不在数据库表中，且不参与数据库相关操作。

### @Enhanced
类注解，可选。
可通过本注解用注解方式来自定义表名字、映射后Bean ID。
仅限自动扫描方式有效。

## Service层
使用EnhancedDao无需继续使用GenericService套类，故Service可专注于缩写逻辑，不要再把各种数据封装逻辑上浮至Controller。  
因为很可能各dao的类型相同，故在织入Service使用的时候，请注意使用@Resource或by-Name的@Autowired注解。

## 缓存
经过实际的使用，发现虽然老的GenericDao钩了get/update/delete方法实现了基本缓存，但实际用途并不太大，
缓存放在Service进行重点管理一般可满足大部分使用，可考虑在Service层采用@ClassCacheAble注解配合方法级的@Cacheable

## 测试
目前可以扩大范围使用，尤其适合轻量化重构工作。


