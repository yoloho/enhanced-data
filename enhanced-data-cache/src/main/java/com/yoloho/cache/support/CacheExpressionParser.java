package com.yoloho.cache.support;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.Expression;

public class CacheExpressionParser extends CachedExpressionEvaluator {
    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);
    
    public Object parse(String keyExpression, AnnotatedElementKey methodKey, Method method, Object[] args) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(null, method,
                args, getParameterNameDiscoverer());
        return getExpression(expressionCache, methodKey, keyExpression).getValue(evaluationContext);
    }
}
