package com.yoloho.enhanced.data.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.enhanced.data.dao.api.IgnoreKey;
import com.yoloho.enhanced.data.dao.util.ColumnUtil;

public class ColumnUtilTest {
    @SuppressWarnings("unused")
    private static class Demo {
        String id;
        String displayName;
        @IgnoreKey
        String tmpName;
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getDisplayName() {
            return displayName;
        }
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        public String getTmpName() {
            return tmpName;
        }
        public void setTmpName(String tmpName) {
            this.tmpName = tmpName;
        }
        
    }
    
    public void isFieldName() {
        assertFalse(ColumnUtil.isFieldName(null));
        assertFalse(ColumnUtil.isFieldName(""));
        assertFalse(ColumnUtil.isFieldName(" "));
        assertFalse(ColumnUtil.isFieldName(" abc_abc"));
        assertTrue(ColumnUtil.isFieldName("abc_"));
        assertTrue(ColumnUtil.isFieldName("abc_adfrf"));
        assertTrue(ColumnUtil.isFieldName("_abc_adfrf"));
    }
    
    @Test
    public void parseColumnNamesTest() {
        String str = "@__self__@ + 1";
        try {
            ColumnUtil.parseColumnNames(null, str, Demo.class);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
        }
        Assert.assertEquals("`id` + 1", ColumnUtil.parseColumnNames("id", str, Demo.class));
        Assert.assertEquals("`display_name` + 1", ColumnUtil.parseColumnNames("displayName", str, Demo.class));
        Assert.assertEquals("@__self__@ + 1", ColumnUtil.parseColumnNames("tmpName", str, Demo.class));
        str = "@id@ + 1";
        Assert.assertEquals("`id` + 1", ColumnUtil.parseColumnNames(null, str, Demo.class));
        Assert.assertEquals("`id` + 1", ColumnUtil.parseColumnNames("displayName", str, Demo.class));
        str = "round(@id@)";
        Assert.assertEquals("round(`id`)", ColumnUtil.parseColumnNames(null, str, Demo.class));
        Assert.assertEquals("round(`id`)", ColumnUtil.parseColumnNames("displayName", str, Demo.class));
        str = "@tmpName@ + 1";
        Assert.assertEquals("@tmpName@ + 1", ColumnUtil.parseColumnNames(null, str, Demo.class));
    }

}
