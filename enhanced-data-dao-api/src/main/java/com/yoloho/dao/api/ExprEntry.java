package com.yoloho.dao.api;

import java.io.Serializable;

/**
 * 表达式条件
 * <p>
 * <b>注意，本类有注入风险，需要严格控制及考虑语法</b>
 * 
 * @author jason<jason@dayima.com> @ May 29, 2018
 *
 */
public class ExprEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String value;
    private Class<?> clz;
    
    /**
     * 需要引用其它列时，@fieldName@，不能直接指定为列的数据库名字
     * 需要引用当前的列时，@__self__@
     * <p>
     * <b>本类有注入风险，需要严格控制及考虑语法</b>
     * 
     * @param value
     *          表达式
     * @param clz
     *          DynamicQueryFilter所绑定的类
     * 
     */
    public ExprEntry(String value, Class<?> clz) {
        setValue(value);
        setClz(clz);
    }
    
    public String getValue() {
        return value;
    }
    
    public Class<?> getClz() {
        return clz;
    }
    
    /**
     * 要更新的值
     * 
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    public void setClz(Class<?> clz) {
        this.clz = clz;
    }
    
}
