package com.yoloho.cache.support;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 启用了缓存注解的方法拦截器
 * 
 * @author jason<jason@dayima.com> @ Jun 6, 2018
 *
 */
public class CacheInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method methodImplemented = CacheProcessor.getMethodUnderProxy(invocation);
        if (methodImplemented == null) {
            return invocation.proceed();
        }
        List<String> keyList = CacheProcessor.cacheEvictKeys(methodImplemented, invocation.getArguments());
        Object finalValue = CacheProcessor.cacheInvoke(methodImplemented, invocation);
        //处理缓存失效
        if (keyList != null) {
            CacheProcessor.cacheEvict(keyList, methodImplemented);
        }
        return finalValue;
    }
}
