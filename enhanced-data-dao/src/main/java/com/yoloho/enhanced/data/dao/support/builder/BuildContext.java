package com.yoloho.enhanced.data.dao.support.builder;

import com.yoloho.enhanced.data.dao.support.EnhancedConfig;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;

/**
 * Dao Bean构建Context
 * 
 * @author houlf
 */
public class BuildContext {

    private Class<?> clazz;
    private ClassInfo clazzInfo;
    private EnhancedConfig config;

    public BuildContext(Class<?> clazz, ClassInfo clazzInfo, EnhancedConfig config) {
        this.clazz = clazz;
        this.clazzInfo = clazzInfo;
        this.config = config;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public ClassInfo getClazzInfo() {
        return clazzInfo;
    }

    public void setClazzInfo(ClassInfo clazzInfo) {
        this.clazzInfo = clazzInfo;
    }

    public EnhancedConfig getConfig() {
        return config;
    }

}