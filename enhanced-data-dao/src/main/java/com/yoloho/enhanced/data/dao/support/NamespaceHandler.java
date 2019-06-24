package com.yoloho.enhanced.data.dao.support;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("scan", new EnhancedDaoParser());
        registerBeanDefinitionParser("druid-monitor", new EnhancedDaoParser());
    }

}
