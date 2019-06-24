package com.yoloho.enhanced.data.cache.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("init", new InitCacheParser());
    }

}
