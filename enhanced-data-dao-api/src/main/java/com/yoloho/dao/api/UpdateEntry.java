package com.yoloho.dao.api;

import java.io.Serializable;

/**
 * 范围更新时值的设置类，支持裸更新，但尽量小心使用，或通过增加确定的封装方法来不直接在外部实现
 * 
 * @author jason<jason@dayima.com> @ May 29, 2018
 *
 */
public class UpdateEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean plain = false;
    private String value;
    
    public UpdateEntry() {
    }
    
    public UpdateEntry(String value) {
        this(value, false);
    }
    
    public UpdateEntry(String value, boolean plain) {
        setValue(value);
        setPlain(plain);
    }
    
    public boolean isPlain() {
        return plain;
    }
    
    /**
     * 设置值是否是裸值(不当成内容字符串处理)
     * 注意，这里可能产生注入问题，需要小心处理
     * 
     * @param plain
     */
    public void setPlain(boolean plain) {
        this.plain = plain;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * 要更新的值
     * 
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * 自增1
     */
    public UpdateEntry increse() {
        return increse(1);
    }
    
    /**
     * 自减1
     */
    public UpdateEntry decrese() {
        return increse(-1);
    }
    
    /**
     * 增step，可正负
     * 
     * @param step
     */
    public UpdateEntry increse(int step) {
        if (step >= 0) {
            setPlain(true);
            setValue(String.format("@__self__@ + %d", step));
        } else if (step < 0) {
            setPlain(true);
            setValue(String.format("@__self__@ %d", step));
        }
        return this;
    }
}
