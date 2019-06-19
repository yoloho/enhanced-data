package com.yoloho.data.dao.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.yoloho.data.dao.monitor.DruidMonitorInit;

public class DruidMonitorParser extends AbstractSingleBeanDefinitionParser {
    public static class EmptyBean {
    }
    
    @Override
    protected Class<?> getBeanClass(Element element) {
        return EmptyBean.class;
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String projectName = element.getAttribute("projectName");
        String beanName = element.getAttribute("beanName");
        int intervelInSeconds = NumberUtils.toInt(element.getAttribute("intervelInSeconds"));
        if (intervelInSeconds < 30) {
            intervelInSeconds = 60;
        }
        if (StringUtils.isEmpty(projectName)) {
            throw new RuntimeException("Project name should be empty in EnableDruidMonitor");
        }
        DruidMonitorInit.init(parserContext.getRegistry(), projectName, beanName, intervelInSeconds);
    }

}