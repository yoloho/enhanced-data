package com.yoloho.dao.sharding.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.yoloho.common.annotation.NonNull;
import com.yoloho.common.annotation.Nullable;
import com.yoloho.dao.api.filter.QueryData;

public interface ShardedDao<T, PK extends Serializable> {
	
    /**
     * 单个主键查询
     * @param key
     * @return
     */
    @Nullable
    public T get(@NonNull PK key, List<ShardingFactor> shardingFactors);
    
    /**
     * 指定列的值查询，多条匹配只返回第1个，没找到返回null
     * @param name
     * @param val
     * @param shardingFactors
     * @return
     */
    @Nullable
    public <TT> T get(@NonNull String name, @NonNull TT val, List<ShardingFactor> shardingFactors);
    
    
    /**
     * 指定查询条件，多条匹配只返回第1个，没找到返回null
     * @param name
     * @param val
     * @param shardingFactors
     * @return
     */
	public T get(QueryData queryData, List<ShardingFactor> shardingFactors);
    
    /**
     * 根据QueryData查询，返回List<T>
     * @param queryData
     * @param shardingFactors
     * @return
     */
    @NonNull
    public List<T> find(@NonNull QueryData queryData, List<ShardingFactor> shardingFactors);
    
    /**
     * 根据QueryData查询，返回List<Map>
     * @param fields
     * @param queryData
     * @param shardingFactors
     * @return
     */
    @NonNull
    public List<Map<String, Object>> find(String fields, QueryData queryData, List<ShardingFactor> shardingFactors);

    /**
     * 根据QueryData查询数量
     * @param queryData
     * @param shardingFactors
     * @return
     */
    @NonNull
    public int count(@NonNull QueryData queryData, List<ShardingFactor> shardingFactors);
	
    /**
     * 插入
     * @param bean
     * @return
     */
    public int insert(@NonNull T bean);

    /**
     * 插入，重复主键时是否忽略插入
     * @param bean
     * @return
     */
    public int insert(@NonNull T bean, boolean ignore);
    
    /**
     * 批量插入
     * @param beanList
     * @return
     */
    public int insert(@NonNull List<T> beanList);
    
    /**
     * 批量插入
     * @param beanList
     * @return
     */
    public int insert(@NonNull List<T> beanList, boolean ignore);
    
    /**
     * 替换插入
     * @param bean
     * @return affect rows
     * @注意，对于replace来说，至少返回影响行数1，如果大于1，说明有"replace"发生
     */
    public int replace(@NonNull T bean);
    
    /**
     * 插入并返回插入后的bean(尤其适用有自增主键时)
     * @param bean
     * @return
     */
    @NonNull
    public T replaceAndReturn(@NonNull T bean);
    
    /**
     * 批量插入
     * @param beanList
     * @return	affect rows
     * 这里需要注意，对于replace来说，至少返回影响行数1，如果大于1，说明有"replace"发生
     */
    public int replace(@NonNull List<T> beanList);
    
    /**
     * 批量插入并返回插入后的bean(尤其适用有自增主键时)
     * @param beanList
     * @return
     */
    @NonNull
    public List<T> replaceAndReturn(@NonNull List<T> beanList);
    
    /**
     * 更新
     * @param bean
     * @return
     */
    public int update(@NonNull T bean);
    
    /**
     * 批量更新
     * @param beanList
     * @return
     */
    public int update(@NonNull List<T> beanList);
    
    /**
     * 根据主键删除数据
     * @param id
     * @param shardingFactors
     * @return
     */
    public int delete(PK id, List<ShardingFactor> shardingFactors);
    
    /**
     * 根据条件删除数据
     * @param queryData
     * @return
     */
    public int delete(QueryData queryData, List<ShardingFactor> shardingFactors);
    
}