package com.yoloho.dao.api.filter;

import java.util.HashMap;

public class QueryData extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public void setLimit(int limit) {
        if (limit < 1) {
            limit = 1;
        }
        put("LimitStart", 0);
        put("LimitCount", limit);
    }

    public void setLimit(int offset, int limit) {
        if (offset < 0) {
            offset = 0;
        }
        if (limit < 1) {
            limit = 1;
        }
        put("LimitStart", offset);
        put("LimitCount", limit);
    }

    public void setPage(int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 1;
        }
        put("LimitStart", (page - 1) * pageSize);
        put("LimitCount", pageSize);
    }

    public int getOffset() {
        if (containsKey("LimitStart")) {
            return (Integer) get("LimitStart");
        }
        return 0;
    }

    public int getLimit() {
        if (containsKey("LimitCount")) {
            return (Integer) get("LimitCount");
        }
        return 0;
    }
    
    public String getWhere() {
        return (String)get("WhereSQL");
    }
    
    public String getSort() {
        return (String)get("SortSQL");
    }
    
}
