package com.yoloho.data.dao.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.w3c.dom.Element;

import com.yoloho.common.util.JoinerSplitters;
import com.yoloho.data.dao.annotations.EnableEnhancedDao;

public class EnhancedConfig {
    final static private String ATTR_POSTFIX = "postfix";
    final static private String ATTR_SCAN = "scan-path";
    final static private String ATTR_SQL_SESSION_FACTORY = "sql-session-factory";
    
    private List<String> scanPath = Collections.emptyList();
    private String sqlSessionFactory = "sqlSessionFactory";
    private String prefix = "";
    private String postfix = "EnhancedDao";
    
    public EnhancedConfig() {
    }
    
    public EnhancedConfig(Element element) {
        {
            String postfix = element.getAttribute(ATTR_POSTFIX);
            if (StringUtils.isEmpty(postfix)) {
                postfix = "EnhancedDao";
            }
            this.postfix = postfix;
        }
        {
            String path = element.getAttribute(ATTR_SCAN);
            if (StringUtils.isEmpty(path)) {
                throw new RuntimeException("scan path must not be empty");
            }
            this.scanPath = JoinerSplitters.getSplitter(",").splitToList(path);
        }
        {
            String sqlFactoryName = element.getAttribute(ATTR_SQL_SESSION_FACTORY);
            if (StringUtils.isEmpty(sqlFactoryName)) {
                sqlFactoryName = "sqlSessionFactory";
            }
            this.sqlSessionFactory = sqlFactoryName;
        }
    }
    
    public EnhancedConfig(AnnotationMetadata annotationMetadata) {
        Map<String, Object> map = annotationMetadata.getAnnotationAttributes(EnableEnhancedDao.class.getName());
        {
            String prefix = (String)map.get("prefix");
            if (StringUtils.isNotEmpty(prefix)) {
                this.prefix = prefix;
            }
        }
        {
            String postfix = (String)map.get("postfix");
            if (StringUtils.isEmpty(postfix)) {
                postfix = "EnhancedDao";
            }
            this.postfix = postfix;
        }
        {
            String[] path = (String[])map.get("scanPath");
            if (path == null || path.length == 0) {
                throw new RuntimeException("scan path must not be empty");
            }
            this.scanPath = Arrays.asList(path);
        }
        {
            String sqlFactoryName = (String)map.get("sqlSessionFactory");
            if (StringUtils.isEmpty(sqlFactoryName)) {
                sqlFactoryName = "sqlSessionFactory";
            }
            this.sqlSessionFactory = sqlFactoryName;
        }
    }

    public List<String> getScanPath() {
        return scanPath;
    }
    
    public void setScanPath(List<String> scanPath) {
        this.scanPath = scanPath;
    }
    
    public String getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(String sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }
}
