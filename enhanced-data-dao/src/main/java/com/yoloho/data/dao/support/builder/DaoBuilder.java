package com.yoloho.data.dao.support.builder;

import com.yoloho.data.dao.api.EnhancedType;

/**
 * EnhancedDao构造器
 * @author houlf
 *
 */
public interface DaoBuilder {
	
	public EnhancedType getType();
	
	public BeanWrapper build(BuildContext entityInfo, String sqlFactoryName);

}