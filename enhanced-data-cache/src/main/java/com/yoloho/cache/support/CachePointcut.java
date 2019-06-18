package com.yoloho.cache.support;

import java.lang.reflect.Method;

import org.springframework.aop.support.StaticMethodMatcherPointcut;

public class CachePointcut extends StaticMethodMatcherPointcut {
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return CacheProcessor.processMethodAnnotation(method, targetClass);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CachePointcut)) {
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return CachePointcut.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}
