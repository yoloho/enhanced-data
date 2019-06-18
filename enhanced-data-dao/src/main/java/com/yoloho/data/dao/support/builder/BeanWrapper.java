package com.yoloho.data.dao.support.builder;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Dao Bean
 *
 */
public class BeanWrapper {
	
	private String name;
	private BeanDefinition bean;
	
	public static BeanWrapper instance(String name, BeanDefinition bean) {
		BeanWrapper beanDef = new BeanWrapper();
		beanDef.name = name;
		beanDef.bean = bean;
		return beanDef;
	}
	
	public String getName() {
		return this.name;
	}
	
	public BeanDefinition getBean() {
		return this.bean;
	}

}
