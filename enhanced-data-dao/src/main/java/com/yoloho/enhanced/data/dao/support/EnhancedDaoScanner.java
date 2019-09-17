package com.yoloho.enhanced.data.dao.support;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * 扫描包下注解
 * 
 * @author jason
 * 
 */
public class EnhancedDaoScanner {
    private final static Logger logger = LoggerFactory.getLogger(EnhancedDaoScanner.class.getSimpleName());
    private SqlSessionFactory factory;

    public EnhancedDaoScanner(SqlSessionFactory sqlSessionFactory) throws IOException {
        logger.info("register mapper to {}", sqlSessionFactory);
        this.factory = sqlSessionFactory;
        try {
            String[] paths = new String[] {"com/yoloho/enhanced/data/dao/xml/enhanced-dao-generic.xml", "/com/yoloho/enhanced/data/dao/xml/enhanced-dao-generic.xml"};
            InputStream in = null;
            for (int i = 0; i < paths.length; i ++) {
                in = EnhancedDaoScanner.class.getClassLoader().getResourceAsStream(paths[i]);
                if (in != null) {
                    break;
                }
            }
            this.parseStream("enhanced-dao-generic.xml", in);
            in.close();
        } catch (Exception e) {
            logger.error("can not locate generic mapping resource", e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
    
    public void parseStream(String resourceName, InputStream in) {
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(in, this.factory.getConfiguration(),
                resourceName, this.factory.getConfiguration().getSqlFragments());
        xmlMapperBuilder.parse();
    }
    
    public void setMapperLocations(Resource[] resources) {
        for (Resource resource : resources) {
            try (InputStream in = resource.getInputStream()) {
                parseStream(resource.getFilename(), in);
            } catch (IOException e) {
                logger.error("Error to open stream on {}", resource);
            }
        }
    }
}
