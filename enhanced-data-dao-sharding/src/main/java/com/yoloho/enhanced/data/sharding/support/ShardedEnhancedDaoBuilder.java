package com.yoloho.enhanced.data.sharding.support;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.data.dao.api.EnhancedType;
import com.yoloho.enhanced.data.dao.support.builder.BeanWrapper;
import com.yoloho.enhanced.data.dao.support.builder.BuildContext;
import com.yoloho.enhanced.data.dao.support.builder.DaoBuilder;
import com.yoloho.enhanced.data.sharding.annotation.Sharded;
import com.yoloho.enhanced.data.sharding.api.ShardingInfo;
import com.yoloho.enhanced.data.sharding.impl.ShardedDaoImpl;

public class ShardedEnhancedDaoBuilder implements DaoBuilder {
    final static private Logger logger = LoggerFactory.getLogger(ShardedEnhancedDaoBuilder.class);
    
    private BuildContext context;

    @Override
    public EnhancedType getType() {
        return EnhancedType.SHARDED;
    }

    @Override
    public BeanWrapper build(BuildContext buildContext, String sqlFactoryName) {
        this.context = buildContext;
        Class<?> entityClz = buildContext.getClazz();
        Sharded curAnnotation = entityClz.getAnnotation(Sharded.class);
        if (curAnnotation != null) {
            // 获取数据库表分片信息
            ShardingInfo shardedInfo = this.buildShardInfo(curAnnotation, entityClz);
            if (shardedInfo != null) {
                return this.buildBean(shardedInfo, sqlFactoryName);
            } else {
                logger.warn("无法构造ShardedDao BeanDefination，未找到Sharded:{}", entityClz);
            }
        }
        return null;
    }

    /**
     * 构建分片信息
     * 
     * @param shardedAnno
     * @param entityClazz
     */
    private ShardingInfo buildShardInfo(Sharded shardedAnno, Class<?> entityClazz) {
        ShardingInfo shardInfo = new ShardingInfo(entityClazz);
        shardInfo.setDao(this.getDaoName(shardedAnno.dao(), entityClazz.getSimpleName()));
        shardInfo.setTable(this.getTable(shardedAnno.table(), entityClazz.getSimpleName()));
        shardInfo.setStrategy(shardedAnno.strategy());
        shardInfo.setHandler(shardedAnno.handler());
        shardInfo.setMode(shardedAnno.mode());
        return shardInfo;
    }

    /**
     * 获取Dao bean名称
     * 
     * @param dao
     * @param clazzName
     * @return
     */
    private String getDaoName(String dao, String clazzName) {
        if (StringUtils.isNotBlank(dao)) {
            return dao;
        } else {
            return clazzName + "ShardedDao";
        }
    }

    /**
     * 获取Table基础名称：非真实表名，需在实际运行中以此为基础构建真实表名
     * 
     * @param table
     * @param clazzName
     * @return
     */
    private String getTable(String table, String clazzName) {
        if (StringUtils.isNotBlank(table)) {
            return table;
        } else {
            return StringUtil.toUnderline(clazzName);
        }
    }

    /**
     * 构建注册Dao Bean
     * 
     * @param shardedInfo
     * @return
     */
    private BeanWrapper buildBean(ShardingInfo shardedInfo, String sqlFactoryName) {
        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(ShardedDaoImpl.class);
        daoBuilder.addConstructorArgValue(shardedInfo);
        daoBuilder.addConstructorArgReference(sqlFactoryName);
        daoBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        daoBuilder.setRole(BeanDefinition.ROLE_APPLICATION);
        daoBuilder.addDependsOn(context.getScannerBeanName());
        return BeanWrapper.instance(shardedInfo.getDao(), daoBuilder.getBeanDefinition());
    }

}