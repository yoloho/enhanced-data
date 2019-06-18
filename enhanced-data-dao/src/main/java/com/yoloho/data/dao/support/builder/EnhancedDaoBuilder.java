package com.yoloho.data.dao.support.builder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.yoloho.common.util.StringUtil;
import com.yoloho.dao.api.Enhanced;
import com.yoloho.dao.api.EnhancedType;
import com.yoloho.data.dao.impl.EnhancedDaoImpl;
import com.yoloho.data.dao.support.EnhancedDaoConstants;

import io.github.lukehutch.fastclasspathscanner.scanner.AnnotationInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.AnnotationInfo.AnnotationParamValue;

public class EnhancedDaoBuilder implements DaoBuilder{

	@Override
	public EnhancedType getType() {
		return EnhancedType.ENHANCED;
	}

	@Override
	public BeanWrapper build(BuildContext buildContext, String sqlFactoryName) {
        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(EnhancedDaoImpl.class);

        ClassInfo classInfo = buildContext.getClazzInfo();
        List<AnnotationInfo> listAnno = classInfo.getAnnotationInfo();
        String beanName = null;
        String tableName = null;
        
        for (AnnotationInfo annotationInfo : listAnno) {
            if (annotationInfo.getAnnotationType().isAssignableFrom(Enhanced.class)) {
                List<AnnotationParamValue> values = annotationInfo.getAnnotationParamValues();
                if (values != null && values.size() > 0) {
                    for (AnnotationParamValue annotationParamValue : values) {
                        String name = annotationParamValue.getParamName();
                        if (name.equals("name")) {
                            beanName = (String) annotationParamValue.getParamValue();
                        } else if (name.equals("tableName")) {
                            tableName = (String) annotationParamValue.getParamValue();
                        }
                    }
                }
            }
        }
        if (beanName == null || beanName.length() == 0) {
            String simpleName = EnhancedDaoConstants.patternClassName.matcher(classInfo.getClassName()).replaceAll("$1");
            StringBuilder beanNameBuilder = new StringBuilder();
            if (StringUtils.isNotEmpty(buildContext.getConfig().getPrefix())) {
                beanNameBuilder.append(buildContext.getConfig().getPrefix());
            }
            beanNameBuilder.append(StringUtil.toCamel(simpleName)).append(buildContext.getConfig().getPostfix());
            beanName = beanNameBuilder.toString();
        }
        daoBuilder.addConstructorArgValue(classInfo.getClassName());
        daoBuilder.addConstructorArgValue(tableName);
        daoBuilder.addConstructorArgReference(sqlFactoryName);

        return BeanWrapper.instance(beanName, daoBuilder.getBeanDefinition());
	}
	
}