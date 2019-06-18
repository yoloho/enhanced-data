package com.yoloho.data.dao.support;

import java.util.regex.Pattern;

public interface EnhancedDaoConstants {
	
    String ATTR_POSTFIX = "postfix";
    String ATTR_SCAN = "scan-path";
    String ATTR_SQL_SESSION_FACTORY = "sql-session-factory";
    Pattern patternClassName = Pattern.compile("^.*?([a-zA-Z0-9_\\-]+)$");
    
}
