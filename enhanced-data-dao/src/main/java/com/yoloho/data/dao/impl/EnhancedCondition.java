package com.yoloho.data.dao.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.yoloho.dao.api.filter.QueryData;

public class EnhancedCondition extends QueryData {
    private static final long serialVersionUID = 1L;
    /**
     * 为了规避一些sql解析引擎对表名字`table`的识别性所做的临时处理
     */
    @SuppressWarnings("unused")
    private static final List<String> innerKeywords = Lists.newArrayList(
            "select", "delete", "update", "insert", "replace", 
            "create", "alter"
            );

    public EnhancedCondition(String genericFields, String genericTableName) {
        put("genericFields", genericFields);
        //受影响的mycat版本是1.5，1.4/1.6均无此问题，目前先恢复为escape方式
        /*if (innerKeywords.contains(genericTableName)) {
            put("genericTableName", String.format("`%s`", genericTableName));
        } else {*/
        put("genericTableName", genericTableName);
        //}
    }
    
    public EnhancedCondition(EnhancedCondition condition) {
        putAll(condition);
    }
}
