package com.yoloho.data.dao.support;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扫描包下注解
 * 
 * @author jason
 * 
 */
public class EnhancedDaoScanner {
    private final static Logger logger = LoggerFactory.getLogger(EnhancedDaoScanner.class.getSimpleName());

    public EnhancedDaoScanner(SqlSessionFactory sqlSessionFactory) throws IOException {
        logger.info("register mapper to {}", sqlSessionFactory);
        try {
            String[] paths = new String[] {"com/yoloho/mybatis/common/dao/xml/enhanced-dao-generic.xml", "/com/yoloho/mybatis/common/dao/xml/enhanced-dao-generic.xml"};
            InputStream in = null;
            for (int i = 0; i < paths.length; i ++) {
                in = EnhancedDaoScanner.class.getClassLoader().getResourceAsStream(paths[i]);
                if (in != null) {
                    break;
                }
            }
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(in, sqlSessionFactory.getConfiguration(),
                    "enhanced-dao-generic.xml", sqlSessionFactory.getConfiguration().getSqlFragments());
            xmlMapperBuilder.parse();
        } catch (Exception e) {
            logger.error("can not locate generic mapping resource", e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
}
