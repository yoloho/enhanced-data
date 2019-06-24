package com.yoloho.enhanced.data.sharding.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yoloho.enhanced.common.util.ReflectUtils;
import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.data.dao.api.IgnoreKey;
import com.yoloho.enhanced.data.dao.api.PrimaryKey;
import com.yoloho.enhanced.data.dao.api.UnionPrimaryKey;
import com.yoloho.enhanced.data.dao.api.filter.DynamicQueryFilter;
import com.yoloho.enhanced.data.dao.api.filter.QueryData;
import com.yoloho.enhanced.data.dao.generator.GeneratedField;
import com.yoloho.enhanced.data.dao.generator.GeneratedValue;
import com.yoloho.enhanced.data.dao.generator.GeneratorManager;
import com.yoloho.enhanced.data.dao.impl.EnhancedCondition;
import com.yoloho.enhanced.data.sharding.api.Column;
import com.yoloho.enhanced.data.sharding.api.ShardedDao;
import com.yoloho.enhanced.data.sharding.api.ShardingFactor;
import com.yoloho.enhanced.data.sharding.api.ShardingInfo;
import com.yoloho.enhanced.data.sharding.strategy.ShardingContext;
import com.yoloho.enhanced.data.sharding.strategy.ShardingHandlerHolder;
import com.yoloho.enhanced.data.sharding.strategy.ShardingStrategy;

@SuppressWarnings("all")
public class ShardedDaoImpl<T, PK extends Serializable>
		extends SqlSessionDaoSupport implements ShardedDao<T, PK>{
	
	public static final Logger logger = LoggerFactory.getLogger(ShardedDaoImpl.class);
	
    private SqlSessionFactory sqlSessionFactory;
    private Map<String, Column> fieldsMapping = Maps.newConcurrentMap();
    private String fieldsGenerated;
    private Class<T> entityClazz;
    private Set<String> primaryKeys = Sets.newConcurrentHashSet();
    private int batchSize = 200;
    
    private ShardingInfo shardedInfo;										//分表信息
    private Map<String, GeneratedField> generatedFields = new HashMap<>();	//需要要自动生成Value的字段
    
    /**
     * 各namespace常量
     */
    private final static String NAMESPACE = "com.yoloho.enhanced.data.dao.namespace.Generic";
    private final static String NAMESPACE_GET = NAMESPACE + ".get";
    private final static String NAMESPACE_INSERT = NAMESPACE + ".insert";
    private final static String NAMESPACE_DELETE = NAMESPACE + ".delete";
    private final static String NAMESPACE_UPDATE = NAMESPACE + ".update";
    private final static String NAMESPACE_UPDATE_FILETER = NAMESPACE + ".updateByFilter";
    /**
     * 各传递到mapper中map的key标准常量
     */
    private final static String KEY_INSERT = "insertOperation";
    private final static String KEY_PROPERTY_LIST = "properyNameList";
    private final static String KEY_COLUMNS = "columns";
    private final static String KEY_DATA = "data";
    private final static String KEY_AUTO_INCREMENT = "autoIncrementKey";
    
    public ShardedDaoImpl() {}    
    
    public ShardedDaoImpl(ShardingInfo shardedInfo, SqlSessionFactory sqlSessionFactory) {
        if (sqlSessionFactory != null) {
            this.setSqlSessionFactory(sqlSessionFactory);
        }
        
        if(shardedInfo != null) {
        	this.doParseShardedInfo(shardedInfo);
        }
    }
    
    /**
     * 获取分表表名
     * @return
     * @throws Exception 
     */
    public String getShardingTableName(T entity){
    	try {
        	return ShardingHandlerHolder.getShardingTable(ShardingContext.instance(shardedInfo, entity));
    	}catch(Exception exp) {
    		throw new RuntimeException(exp);
    	}
    }
    
    /**
     * 获取分表表名
     * @param shardingFactors 分表因子
     * @return
     */
    public String getShardingTableName(List<ShardingFactor> shardingFactors) {
    	try {
        	return ShardingHandlerHolder.getShardingTable(ShardingContext.instance(shardedInfo, shardingFactors));
    	}catch(Exception exp) {
    		throw new RuntimeException(exp);
    	}
    }
    
    /**
     * 解析分表信息
     * @param shardedInfo
     */
    public void doParseShardedInfo(ShardingInfo shardedInfo) {
        try {
        	//设置Dao所使用的Sharded信息
        	this.shardedInfo = shardedInfo;

        	//注册ShardingHandler
        	if(shardedInfo.getStrategy()==ShardingStrategy.CUSTOM) {
            	ShardingHandlerHolder.registryHandler(shardedInfo.getHandler());
        	}
        	
	        //创建fields容器
	        List<Field> fields = Lists.newArrayList();
	        Set<String> fieldNameSet = Sets.newHashSet();

	        //获取所有的field、fieldName
        	this.entityClazz = (Class<T>) shardedInfo.getEntityClazz();
	        Class<?> curclz = entityClazz;
	        while(curclz != null) {
	            Field[] arr = curclz.getDeclaredFields();
	            for (Field field : arr) {
	                if (fieldNameSet.contains(field.getName())) {
	                    continue;					//对于被子类覆盖的属性，跳过
	                }
	                if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
	                    continue;					//跳过final和static
	                }
	                fields.add(field);
	                fieldNameSet.add(field.getName());
	            }
	            curclz = curclz.getSuperclass();
	        }
			
	        //解析、构建fieldsMapping、primaryKey、shardedKey
	        boolean hasPrimaryKey=false, hasShardedKey=false;
	        for (Field field : fields) {
	            //目前是不限访问类型，这里可以考虑下是否限定继承链中仅protected/public才计入
	            String fieldName = field.getName();
	            if (fieldName.equals("serialVersionUID")) {
	                continue;
	            }
	            if (field.isAnnotationPresent(IgnoreKey.class)) {
	                continue;
	            }
	            Column column = new Column();
	            if (field.isAnnotationPresent(PrimaryKey.class)) {
	                //primary
	                primaryKeys.add(fieldName);
	                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
	                column.setPrimary(true);
	                column.setAutoIncrement(primaryKey.autoIncrement());
	                hasPrimaryKey = true;
	            }
	            String propertyName = StringUtil.toCamel(fieldName);
	            String columnName = StringUtil.toUnderline(fieldName);
	            column.setColumnName(columnName);
	            column.setPropertyName(propertyName);
	            this.fieldsMapping.put(propertyName, column);
	            
	            //解析字段
	            if(field.isAnnotationPresent(GeneratedValue.class)) {
	            	generatedFields.put(field.getName(), new GeneratedField(field,field.getAnnotation(GeneratedValue.class)));
	            }
	            
	            logger.debug("field mapping: {} => {} ({})", propertyName, columnName, fieldName);
	        }
	        
	        if(!hasPrimaryKey){
	        	logger.error("未设置PrimaryKey, entity={}", shardedInfo.getEntityClazz());
				throw new RuntimeException("未设置PrimaryKey!");
	        }
	        
	        //generate fields
	        {
	            StringBuffer buffer = new StringBuffer();
	            for (Entry<String, Column> entry : this.fieldsMapping.entrySet()) {
	                Column column = entry.getValue();
	                if (buffer.length() > 0) {
	                    buffer.append(",");
	                }
	                buffer.append('`').append(column.getColumnName()).append('`');
	                if (!column.getPropertyName().equals(column.getColumnName())) {
	                    buffer.append(" as `").append(column.getPropertyName()).append('`');
	                }
	            }
	            fieldsGenerated = buffer.toString();
	            logger.debug("fields: {}", fieldsGenerated);
	        }
		} catch (Exception e) {
			logger.error("解析构建ShardedDao出错，未找到Entity bean={}", shardedInfo.getEntityClazz(), e);
			throw new RuntimeException(e);
		}
    }


	@Override
	public int insert(T bean) {
		return this.insert(bean, false);
	}

	@Override
	public int insert(T bean, boolean ignore) {
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        return insert(beans, ignore);
	}
	
	@Override
	public int insert(List<T> beanList) {
		return this.insert(beanList, false);
	}
    
	@Override
    public int insert(List<T> beanList, boolean ignore) {
        return insert(beanList, ignore, false);
    }
	
	@SuppressWarnings("unchecked")
	private List<T> insertAndReturn(List<T> beanList, boolean ignore, boolean replace) {
		// ignore下，存在被忽略的行时，mybatis会按顺序设置autoincrement id，所以这种情况下不能走list
		if (beanList.size() > 0 && (ignore || replace)) {
			List<T> list = Lists.newArrayList();
			for (T bean : beanList) {
				list.addAll(_insertAndReturn(Lists.newArrayList(bean), ignore, replace));
			}
			return list;
		}
		return _insertAndReturn(beanList, ignore, replace);
	}
	
	@Override
	public int replace(T bean) {
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
		return this.replace(beans);
	}

	@Override
	public int replace(List<T> beanList) {
        return insert(beanList, false, true);
	}

	@Override
	public T replaceAndReturn(T bean) {
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        beans = replaceAndReturn(beans);
        return beans.get(0);
	}

	@Override
	public List<T> replaceAndReturn(List<T> beanList) {
        return insertAndReturn(beanList, false, true);
	}
	
	@Override
	public int update(T bean) {
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
		return this.update(beans);
	}

	@Override
	public int update(List<T> beanList) {
        int count = 0;
        List<String> properyNameList = Lists.newArrayList();
        for (Entry<String, Column> entry : fieldsMapping.entrySet()) {
            //待更新的属性跳过主键
            if (entry.getValue().isPrimary()) {
                continue;
            }
            properyNameList.add(entry.getKey());
        }

        if (beanList.size() == 1) {
            // 单条不开启batch
            T bean = beanList.get(0);
        	//获取分表表名，并构建EnhancedCondition
            EnhancedCondition dataBase = this.getEnhancedCondition4Update(bean, properyNameList);
            dataBase.put(KEY_DATA, bean);
            dataBase.putAll(this.getPrimaryKeyCondition(bean));
            count += getSqlSession().update(NAMESPACE_UPDATE, dataBase);
        } else {
            // 批量更新
            int len = beanList.size();
            List<Map<String, Object>> dataList = Lists.newArrayListWithCapacity(len);
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                int cur = 0;
                for (T bean : beanList) {
                	//获取分表表名，并构建EnhancedCondition
                    EnhancedCondition dataBase = this.getEnhancedCondition4Update(bean, properyNameList);

                	Map<String, Object> data = Maps.newHashMap();
                    dataList.add(data);
                    data.putAll(dataBase);
                    data.put(KEY_DATA, bean);
                    data.putAll(getPrimaryKeyCondition(bean));
                    session.update(NAMESPACE_UPDATE, data);
                    cur ++;
                    if (cur % batchSize == 0) {
                        //reach batch
                        List<BatchResult> results = session.flushStatements();
                        if (results.size() > 0) {
                            BatchResult batchResult = results.get(0);
                            for (int num : batchResult.getUpdateCounts()) {
                                count += num;
                            }
                            logger.debug("reach a batch");
                        }
                    }
                }
                List<BatchResult> results = session.flushStatements();
                if (results.size() > 0) {
                    BatchResult batchResult = results.get(0);
                    for (int num : batchResult.getUpdateCounts()) {
                        count += num;
                    }
                }
                session.commit();
            }
        }
        return count;
	}
	
	
	@Override
	public int delete(PK id, List<ShardingFactor> shardingFactors) {
		QueryData queryData = this.getPrimaryQueryData(id);
		return this.delete(queryData, shardingFactors);
	}

	@Override
	public int delete(QueryData queryData, List<ShardingFactor> shardingFactors) {
        try {
            //单次最大删除行数硬限制
            if (queryData.getLimit() > 1000) {
                queryData.setLimit(1000);
            }
            
        	String shardingTable = this.getShardingTableName(shardingFactors);
            EnhancedCondition condition = new EnhancedCondition("", shardingTable);
            condition.putAll(queryData);
            return getSqlSession().delete(NAMESPACE_DELETE, condition);
        } catch (Exception e) {
            logger.error("batchRemove异常", e);
            throw new RuntimeException("操作失败");
        }
	}
	
	@Override
	public T get(PK key, List<ShardingFactor> shardingFactors) {
		return this.get(this.getPrimaryQueryData(key), shardingFactors);
	}

	@Override
	public <TT> T get(String name, TT val, List<ShardingFactor> shardingFactors) {
        DynamicQueryFilter filter = new DynamicQueryFilter();
        filter.equalPair(name, val);
        return this.get(filter.getQueryData(), shardingFactors);
	}
	
	@Override
	public T get(QueryData queryData, List<ShardingFactor> shardingFactors) {
		queryData.setLimit(1);
		List<T> listDatas = this.find(queryData, shardingFactors);
		return (listDatas==null || listDatas.isEmpty()) ? null : listDatas.get(0);
	}

	@Override
	public List<T> find(QueryData queryData, List<ShardingFactor> shardingFactors) {
        List<Map<String, Object>> mapList = this.find(fieldsGenerated, queryData, shardingFactors);
        List<T> list = Lists.newArrayListWithCapacity(mapList.size());
        for (Map<String, Object> map : mapList) {
            list.add(((JSONObject)JSON.toJSON(map)).toJavaObject(entityClazz));
        }
        return list;
	}
	
    @Override
    public List<Map<String, Object>> find(String fields, QueryData queryData, List<ShardingFactor> shardingFactors) {
    	String shardingTable = this.getShardingTableName(shardingFactors);
        return this.find(fields, queryData, shardingTable);
    }
    
	@Override
	public int count(QueryData queryData, List<ShardingFactor> shardingFactors) {
        try {
        	String shardingTable = this.getShardingTableName(shardingFactors);
            EnhancedCondition condition = new EnhancedCondition("count(1) as count", shardingTable);
            condition.putAll(queryData);
            Map<String, Object> map = getSqlSession().<Map<String, Object>>selectOne(NAMESPACE_GET, condition);
            if (map != null) {
                return NumberUtils.toInt(map.get("count").toString(), 0);
            }
            return 0;
        } catch (Exception e) {
            logger.error("count异常", e);
            throw new RuntimeException("操作失败");
        }
	}
	
    private List<Map<String, Object>> find(String fields, QueryData queryData, String shardingTable) {
        EnhancedCondition condition = new EnhancedCondition(fields, shardingTable);
        condition.putAll(queryData);
        List<Map<String, Object>> mapList = getSqlSession().<Map<String, Object>>selectList(NAMESPACE_GET, condition);
        if (mapList == null || mapList.size() == 0) {
            return Collections.emptyList();
        }
        return mapList;
    }

    private int insert(List<T> beanList, boolean ignore, boolean replace) {
        int count = 0;
        List<String> insertProperyNameList = Lists.newArrayList();
        for (Entry<String, Column> entry : fieldsMapping.entrySet()) {
            insertProperyNameList.add(entry.getKey());
        }
        
        if (beanList.size() == 1) {
            //单条不开启batch
            T bean = beanList.get(0);
			//新增预处理Entity
			this.preHandle4Insert(bean);
            //获取分表表名，并构建EnhancedCondition
            EnhancedCondition dataBase = this.getCondition4Insert(bean, insertProperyNameList, ignore, replace);
            dataBase.put(KEY_DATA, bean);
            count += getSqlSession().insert(NAMESPACE_INSERT, dataBase);
        } else {
        	//批量插入
            int len = beanList.size();
            List<EnhancedCondition> dataList = Lists.newArrayListWithCapacity(len);
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                int cur = 0;
                for (T bean : beanList) {
					//新增预处理Entity
					this.preHandle4Insert(bean);
                	
                	//获取分表表名，并构建EnhancedCondition
                    EnhancedCondition data = this.getCondition4Insert(bean, insertProperyNameList, ignore, replace);
                    
                    dataList.add(data);
                    data.put(KEY_DATA, bean);
                    session.insert(NAMESPACE_INSERT, data);
                    cur ++;
                    if (cur % batchSize == 0) {
                        //reach batch
                        List<BatchResult> results = session.flushStatements();
                        if (results.size() > 0) {
                            BatchResult batchResult = results.get(0);
                            for (int num : batchResult.getUpdateCounts()) {
                                count += num;
                            }
                            logger.debug("reach a batch");
                        }
                    }
                }
                List<BatchResult> results = session.flushStatements();
                if (results.size() > 0) {
                    BatchResult batchResult = results.get(0);
                    for (int num : batchResult.getUpdateCounts()) {
                        count += num;
                    }
                }
                session.commit();
            }
        }
        return count;
    }

	@SuppressWarnings("unchecked")
	private List<T> _insertAndReturn(List<T> beanList, boolean ignore, boolean replace) {
		try {
			List<String> insertPropertyNames = Lists.newArrayList();
			for (Entry<String, Column> entry : fieldsMapping.entrySet()) {
				insertPropertyNames.add(entry.getKey());
			}

			if (beanList.size() == 1) {
				//单条不开启batch
				T bean = beanList.get(0);
				//新增预处理Entity
				this.preHandle4Insert(bean);
	        	//获取分表表名，并构建EnhancedCondition
	            EnhancedCondition dataBase = this.getCondition4Insert(bean, insertPropertyNames, ignore, replace);
				dataBase.put(KEY_DATA, bean);

				int count = getSqlSession().insert(NAMESPACE_INSERT, dataBase);
				if (count > 0 && dataBase.containsKey(KEY_AUTO_INCREMENT)) {
					long autoIncrementKey = NumberUtils.toLong(dataBase.get(KEY_AUTO_INCREMENT).toString(), 0);
					if (autoIncrementKey > 0) {
						setAutoIncrementField(bean, autoIncrementKey);
					}
				}
			} else {
				// batch
				int len = beanList.size();
				List<EnhancedCondition> dataList = Lists.newArrayListWithCapacity(len);
				try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
					int last = 0;
					int cur = 0;
					for (T bean : beanList) {
						//新增预处理Entity
						this.preHandle4Insert(bean);
			        	//获取分表表名，并构建EnhancedCondition
			            EnhancedCondition data = this.getCondition4Insert(bean, insertPropertyNames, ignore, replace);
						dataList.add(data);
						data.put(KEY_DATA, bean);
						session.insert(NAMESPACE_INSERT, data);
						cur++;
						if (cur % batchSize == 0) {
							logger.info("reach a batch");
							List<BatchResult> results = session.flushStatements();
							if (results.size() > 0) {
								BatchResult batchResult = results.get(0);
								int count = batchResult.getParameterObjects().size();
								for (int i = 0; i < count; i++) {
									Map<String, Object> dataReturn = (Map<String, Object>) batchResult
											.getParameterObjects().get(i);
									if (dataReturn.containsKey(KEY_AUTO_INCREMENT)) {
										long autoIncrementKey = NumberUtils
												.toLong(dataReturn.get(KEY_AUTO_INCREMENT).toString(), 0);
										if (autoIncrementKey > 0) {
											setAutoIncrementField(beanList.get(i + last), autoIncrementKey);
										}
									}
								}
							}
							last = cur;
						}
					}
					List<BatchResult> results = session.flushStatements();
					if (results.size() > 0) {
						BatchResult batchResult = results.get(0);
						int count = batchResult.getParameterObjects().size();
						for (int i = 0; i < count; i++) {
							Map<String, Object> dataReturn = (Map<String, Object>) batchResult.getParameterObjects()
									.get(i);
							if (dataReturn.containsKey(KEY_AUTO_INCREMENT)) {
								long autoIncrementKey = NumberUtils
										.toLong(dataReturn.get(KEY_AUTO_INCREMENT).toString(), 0);
								if (autoIncrementKey > 0) {
									setAutoIncrementField(beanList.get(i + last), autoIncrementKey);
								}
							}
						}
					}
					session.commit();
				}
			}
			return beanList;
		} catch (Exception e) {
			logger.error("batchInsertAndReturn异常", e);
			throw new RuntimeException("操作失败");
		}
	}
    
	/**
	 * 构建分表EnhancedCondition
	 * @param shardedTable
	 * @param ignore
	 * @param replace
	 */
	private EnhancedCondition getCondition4Insert(T entity, List<String> insertProperyNameList,
			boolean ignore, boolean replace) {
		String shardedTable = this.getShardingTableName(entity);
        EnhancedCondition dataBase = new EnhancedCondition("", shardedTable);
        dataBase.put(KEY_PROPERTY_LIST, insertProperyNameList);
        dataBase.put(KEY_COLUMNS, fieldsMapping);
        if (replace) {
            dataBase.put(KEY_INSERT, "replace into");
        } else {
            if (ignore) {
                dataBase.put(KEY_INSERT, "insert ignore");
            } else {
                dataBase.put(KEY_INSERT, "insert");
            }
        }
        return dataBase;
	}
	
	/**
	 * 构建分表EnhancedCondition
	 * @param entity
	 * @param properyNameList 待更新的字段列表
	 */
	private EnhancedCondition getEnhancedCondition4Update(T entity, List<String> properyNameList) {
		String shardedTable = this.getShardingTableName(entity);
        EnhancedCondition dataBase = new EnhancedCondition("", shardedTable);
        dataBase.put(KEY_PROPERTY_LIST, properyNameList);
        dataBase.put(KEY_COLUMNS, fieldsMapping);
        return dataBase;
	}
	
	/**
	 * 设置自增长ID字段
	 * @param bean
	 * @param autoIncrementKey
	 */
    private void setAutoIncrementField(T bean, long autoIncrementKey) {
        for (String key : primaryKeys) {
            try {
                if (fieldsMapping.containsKey(key)) {
                    Column column = fieldsMapping.get(key);
                    if (column.isAutoIncrement()) {
                        Field field = bean.getClass().getDeclaredField(column.getPropertyName());
                        if (field != null) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            if (field.getType() == int.class) {
                                field.setInt(bean, (int) autoIncrementKey);
                            } else if (Integer.class.isAssignableFrom(field.getType())) {
                                field.set(bean, new Integer((int)autoIncrementKey));
                            } else if (field.getType() == long.class) {
                                field.setLong(bean, autoIncrementKey);
                            } else if (Long.class.isAssignableFrom(field.getType())) {
                                field.set(bean, new Long(autoIncrementKey));
                            } else if (field.getType() == short.class) {
                                field.setShort(bean, (short) autoIncrementKey);
                            } else if (Short.class.isAssignableFrom(field.getType())) {
                                field.set(bean, new Short((short) autoIncrementKey));
                            } else if (field.getType() == byte.class) {
                                field.setByte(bean, (byte) autoIncrementKey);
                            } else if (Byte.class.isAssignableFrom(field.getType())) {
                                field.set(bean, new Byte((byte) autoIncrementKey));
                            } else if (field.getType() == char.class) {
                                field.setChar(bean, (char) autoIncrementKey);
                            } else if (Character.class.isAssignableFrom(field.getType())) {
                                field.set(bean, new Character((char) autoIncrementKey));
                            }
                        }
                        break;
                    }
                }
            } catch (NoSuchFieldException e) {
            } catch (SecurityException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }
    
    private QueryData getPrimaryKeyCondition(T bean) {
        DynamicQueryFilter filter = new DynamicQueryFilter();
        for (String propertyName : primaryKeys) {
            Object val = this.getFieldValue(propertyName, bean);
            if (val == null) {
                throw new RuntimeException("error when get primary field's value");
            }
            if (val.getClass() == int.class) {
                filter.equalPair(propertyName, (int)val);
            } else if (val.getClass() == long.class) {
                filter.equalPair(propertyName, (long)val);
            } else if (Number.class.isAssignableFrom(val.getClass())) {
                filter.equalPair(propertyName, (Number)val);
            } else if (String.class.isAssignableFrom(val.getClass())) {
                filter.equalPair(propertyName, (String)val);
            } else {
                filter.equalPair(propertyName, String.valueOf(val));
            }
        }
        filter.limit(1);
        return filter.getQueryData();
    }
    
    private QueryData getPrimaryQueryData(PK key) {
        if (key == null) {
            throw new RuntimeException("主键不能为null");
        }
        DynamicQueryFilter filter = new DynamicQueryFilter();
        if (key instanceof UnionPrimaryKey) {
            //union key
            UnionPrimaryKey primaryValues = (UnionPrimaryKey)key;
            for (String name : primaryKeys) {
                filter.equalPair(name, primaryValues.get(name).toString());
            }
        } else {
            //simple key
            for (String name : primaryKeys) {
                //这里暂时先简单粗暴地处理掉
                filter.equalPair(name, key.toString());
            }
        }
        filter.limit(1);
        return filter.getQueryData();
    }

    private Object getFieldValue(String propertyName, T bean) {
        try {
            Field field = bean.getClass().getDeclaredField(propertyName);
            if (field != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field.get(bean);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            logger.error("can not get field: {}", propertyName, e);
        }
        return null;
    }

    /**
     * 新增前处理
     * @param entity
     */
    private void preHandle4Insert(T entity) {
    	if(generatedFields.size() > 0) {
    		Collection<GeneratedField> generatedFieldCollection = generatedFields.values();
    		for(GeneratedField generatedField : generatedFieldCollection) {
    			if(ReflectUtils.getValue(generatedField.getField(), entity)==null) {
        			GeneratorManager.fillField(entity, generatedField);
    			}
    		}
    	}
    }
    
}