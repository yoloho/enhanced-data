package com.yoloho.data.dao.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.yoloho.common.annotation.NonNull;
import com.yoloho.common.annotation.Nullable;
import com.yoloho.data.dao.api.filter.QueryData;

/**
 * 增强dao访问组件
 * 
 * 注意，对于简单访问的dao，不建议直接暴露在dubbo层，使用本组件，老的GenericService可以弃用
 * Service可以定义成纯粹的Service，并在Service中使用EnhancedDao
 * 
 * @author jason<jason@dayima.com> @ Jun 4, 2018
 *
 * @param <T>
 * @param <PK>
 */
public interface EnhancedDao<T, PK extends Serializable> {
    /**
     * 插入
     * 
     * @param bean
     * @return
     */
    public int insert(@NonNull T bean);
    
    /**
     * 插入，重复主键时是否忽略插入
     * 
     * @param bean
     * @return
     */
    public int insert(@NonNull T bean, boolean ignore);
    
    /**
     * 插入并返回插入后的bean(尤其适用有自增主键时)
     * 
     * @param bean
     * @return
     */
    @NonNull
    public T insertAndReturn(@NonNull T bean);
    
    /**
     * 插入并返回插入后的bean(尤其适用有自增主键时)
     * 
     * @param bean
     * @return
     */
    @NonNull
    public T insertAndReturn(@NonNull T bean, boolean ignore);

    /**
     * 批量插入
     * 
     * @param beanList
     * @return
     */
    public int insert(@NonNull List<T> beanList);
    
    /**
     * 批量插入
     * 
     * @param beanList
     * @return
     */
    public int insert(@NonNull List<T> beanList, boolean ignore);
    
    /**
     * 批量插入并返回插入后的bean(尤其适用有自增主键时)
     * 
     * @param beanList
     * @return
     */
    @NonNull
    public List<T> insertAndReturn(@NonNull List<T> beanList);
    
    /**
     * 批量插入并返回插入后的bean(尤其适用有自增主键时)
     * 
     * @param beanList
     * @return
     */
    @NonNull
    public List<T> insertAndReturn(@NonNull List<T> beanList, boolean ignore);
    
    /**
     * 替换插入
     * 
     * @param bean
     * @return 
     *      affect rows
     *      <p>
     *      这里需要注意，对于replace来说，至少返回影响行数1，如果大于1，说明有"replace"发生
     */
    public int replace(@NonNull T bean);
    
    /**
     * 插入并返回插入后的bean(尤其适用有自增主键时)
     * 
     * @param bean
     * @return
     */
    @NonNull
    public T replaceAndReturn(@NonNull T bean);
    
    /**
     * 批量插入
     * 
     * @param beanList
     * @return
     *      affect rows
     *      <p>
     *      这里需要注意，对于replace来说，至少返回影响行数1，如果大于1，说明有"replace"发生
     */
    public int replace(@NonNull List<T> beanList);
    
    /**
     * 批量插入并返回插入后的bean(尤其适用有自增主键时)
     * 
     * @param beanList
     * @return
     */
    @NonNull
    public List<T> replaceAndReturn(@NonNull List<T> beanList);
    
    /**
     * 主键删除
     * 
     * @param key
     * @return
     */
    public int remove(@SuppressWarnings("unchecked") @NonNull PK... keys);

    /**
     * 根据条件删除
     * 目前在批量删除为了安全，最多一次删除行数被限制为了1000，后续根据实际使用场景进行调整
     * 
     * @param queryData
     * @return
     */
    public int remove(@NonNull QueryData queryData);

    /**
     * 
     * @param keyList
     * @return
     */
    public int remove(@NonNull List<PK> keyList);

    /**
     * 多个主键查询
     * 
     * @param key
     * @return
     */
    @NonNull
    public List<T> find(@SuppressWarnings("unchecked") @NonNull PK... keys);
    
    /**
     * 多个主键查询
     * 
     * @param key
     * @return
     */
    @NonNull
    public List<T> find(@NonNull List<PK> keys);
    
    /**
     * 
     * @param queryData
     * @return
     */
    @NonNull
    public List<T> find(@NonNull QueryData queryData);
    
    /**
     * 根据指定列值查询<br>
     * 注意，在使用可空返回的聚合（而且仅聚合）时，会返回[null]，也就是
     * 列表中出现了空值，带其它列时会返回少了属性的对象如[{label:1}]，正常
     * 是[{label:1, sum:1111}]两个属性的这种
     * 
     * @param name
     * @param val
     * @return
     */
    @NonNull
    public <TT> List<T> find(@NonNull String name, @NonNull TT val, int limit);
    
    /**
     * 指定列来查询，注意，本方法因为可以直接定义查询列，尽量避免使用，试验性功能
     * 
     * @param fields 指定查询列
     * @param queryData
     * @return
     */
    @NonNull
    public List<Map<String, Object>> find(@NonNull String fields, @NonNull QueryData queryData);

    /**
     * 单个主键查询
     * 
     * @param key
     * @return
     */
    @Nullable
    public T get(@NonNull PK key);
    
    /**
     * 单个查询
     * 
     * @param queryData
     * @return
     */
    @Nullable
    public T get(@NonNull QueryData queryData);
    
    /**
     * 指定列的值查询，多条匹配只返回第1个，没找到返回null
     * 
     * @param name
     * @param val
     * @return
     */
    @Nullable
    public <TT> T get(@NonNull String name, @NonNull TT val);
    
    /**
     * 单列条件计数
     * 
     * @param fieldName
     * @param value
     * @return
     */
    public <TT> int count(@NonNull String fieldName, @NonNull TT value);
    
    /**
     * 查询总数
     * 
     * @param Map
     *            queryData
     * @return
     */
    public int count(@NonNull QueryData queryData);
    
    /**
     * sum总数
     * 
     * @param Map
     *            queryData
     * @return
     */
    public int sum(@NonNull String fieldName, @NonNull QueryData queryData);

    /**
     * 修改
     * 
     * @param bean
     * @return
     */
    public int update(@NonNull T bean);

    /**
     * 根据条件修改
     * 试验性功能
     * 
     * @param bean
     * @param queryData
     * @return
     */
    public int update(@NonNull Map<String, UpdateEntry> data, @NonNull QueryData queryData);

    /**
     * 批量更新
     * 
     * @param beanList
     * @return
     */
    public int update(@NonNull List<T> beanList);
    
    /**
     * 获取sqlSession，用于自定义操作
     * <p>
     * 这里为了规避dao-api依赖mybatis包，先用范型声明
     * 
     * @return
     */
    SqlSession getSqlSession();
}
