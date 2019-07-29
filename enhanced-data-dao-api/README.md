enhanced-data-dao-api
===
For generic interfaces / utilities for `EnhancedDao`.

# Core Class
* DynamicQueryFilter
* Enhanced
* PrimaryKey
* IgnoreKey

## Tips
### Tinyint Shrinks into Boolean
JDBC has a feature which can automatically convert tinyint(1) columns into boolean type when saving to db or getting from db. To turn off this you could add following into the JDBC connection string:

```
tinyInt1isBit=false
```

# DynamaicQueryFilter
The core query utility supporting chain's style code like:

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

## Conditions
### equalPair(String fieldName, T value)
It represents the `=`.

Same as:
* notEqual `<>`
* greatThan `>`
* greatOrEqual `>=`
* lessThan `<`
* lessOrEqual `<=`
* in `in (...)`
* notIn `not in (...)`
* like `like '%...%'`
* startsWith `like '...%'`
* endsWith `like '%...'`
* isNull `is null`
* notNull `is not null`

### inJoinedString
For the column having values like `1,2,3,67` to query whether `3` is in it or not.

### expr
Introducing a new type `ExprEntry`.

By this you can do `UNSAFE` but useful operations like `a=a+1`, `a+b>c`. 

Please check the logic carefully and better not to use it.

### addFilter(String fieldName, Operator operator, Type type, Object value)
Which can specify operator manually.

### and() / or()
Specify the logic is `and` or `or` when joining the conditions.

### addSubFilter(DynamicQueryFilter filter)
Append a sub filter.

### addToFilter(DynamicQueryFilter filter)
Add current filter into a parent.

### getQueryData()
Get the `QueryData`.


## Order

### orderBy(String orderBy, boolean isDesc)
Order of `orderBy` matters.

## Page

### limit(int count)
Like `limit xxx`.

### limit(int offset, int count)
Like `limit m, n`.

### page(int page, int pageSize)
Like `limit (p-1)*page, page`.

## Example
```java
long curTime = System.currentTimeMillis();
DynamicQueryFilter filter = new DynamicQueryFilter();
filter.equalPair("status", BaseStatusEnum.VALID.getVal())
    .lessOrEqual("startTime", curTime)
    .greatOrEqual("endTime", curTime)
    .orderBy("showOrder", true)
    .orderBy("regionalId", true)
    .page(1, 20);
List<MallRegional> list = mallRegionalServiceDao.find(filter.getQueryData());
```

# EnhancedDao
How to define a model and generate the dao beans please refer to [enhanced-data-dao](../enhanced-data-dao)



