package com.yoloho.enhanced.data.dao.api.filter;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yoloho.enhanced.common.util.JoinerSplitters;
import com.yoloho.enhanced.data.dao.api.ExprEntry;
import com.yoloho.enhanced.data.dao.api.IgnoreKey;
import com.yoloho.enhanced.data.dao.api.filter.FieldCommand.Operator;
import com.yoloho.enhanced.data.dao.api.filter.FieldCommand.Type;

public class DynamicQueryFilterTest {
    private static void _dumpResult(String title, DynamicQueryFilter filter, String sql) {
        HashMap<String, Object> params = new HashMap<>(filter.getParameters());
        params.remove("WhereSQL");
        System.out.println(title);
        System.out.println("\t" + params);
        System.out.println("\twhere: " + filter.getParameters(sql).get("WhereSQL"));
        System.out.println("\tsort: " + filter.getParameters(sql).get("SortSQL"));
        System.out.println("\t" + filter.getParamValueList());
    }
    
    private static void _dumpResult(String title, DynamicQueryFilter filter) {
        _dumpResult(title, filter, "");
    }
    
    @Test
    public void baseTest() {
      //快捷方法调用测试
        DynamicQueryFilter filter = new DynamicQueryFilter();
        filter.equalPair("groupId", 1);
        filter.equalPair("uid", 123123L);
        filter.equalPair("price1", 1.2F);
        filter.equalPair("price2", 1.22734);
        filter.equalPair("price", new BigDecimal("3.445"));
        filter.equalPair("begin", new Date());
        filter.in("type", Lists.newArrayList(1, 3, 4, 5));
        filter.in("type1", Sets.newHashSet(1, 3));
        filter.equalPair("title", "test");
        System.out.println(filter.getQueryData());
    }
    
    @Test
    public void base1Test() {
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.addFilter("groupId", Operator.equal, Type.Number, 1);
            System.out.println(filter.getParameters(" and newId = 3"));
        }
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            List<Integer> ids = new ArrayList<>();
            ids.add(1);
            ids.add(10);
            ids.add(8);
            ids.add(3);
            ids.add(9);
            filter.addFilter("label", Operator.greatOrEqual, Type.Number, 3)
                .addFilter("groupId", Operator.in, Type.List, ids)
                .addFilter("name", Operator.isNull, Type.String, null)
                .addFilter("topicId", Operator.notIn, Type.List, ids);
            _dumpResult("new method", filter);
        }
        {
            //in 测试
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.in("groupId", JoinerSplitters.getSplitter(",").splitToList("asdf,asdf,d,f,g,g,dsfa"));
            _dumpResult("老in 测试", filter);
        }
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.equalPair("label", "test");
            filter.isNull("sublabel");
            _dumpResult("带null测试", filter);
        }
        
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.equalPair("label", "test");
            filter.equalPair("sublabel", "");
            _dumpResult("Test with empty string", filter);
        }
    }
    @Test
    public void complexTest() {
        //复杂查询测试
        DynamicQueryFilter filter1 = new DynamicQueryFilter();
        filter1.addFilter("hidden", Operator.equal, Type.Number, 0L);
        
        DynamicQueryFilter filter2 = new DynamicQueryFilter();
        filter2.equalPair("hidden", "1");
        filter2.notEqual("uid", 109);
        
        DynamicQueryFilter filterJoin = new DynamicQueryFilter();
        filterJoin.or()
            .addSubFilter(filter1)
            .addSubFilter(filter2);
        
        DynamicQueryFilter filter = new DynamicQueryFilter();
        filter.equalPair("a", "test2");
        filter.equalPair("a", "test1");
        filter.equalPair("b", "ddd");
        filter.addSubFilter(filterJoin);
        _dumpResult("Test with sub conditions", filter);
    }
    
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
    
    /**
     * 带表达式的测试
     * 列的引用需要以 (@fieldName@) 形式
     */
    @Test
    public void exprTest() {
        DynamicQueryFilter filter = new DynamicQueryFilter();
        filter.expr("id", Operator.equal, new ExprEntry("@displayName@ + 1", Demo.class));
        Assert.assertEquals("`id` = `display_name` + 1", filter.getQueryData().getWhere());
        filter = new DynamicQueryFilter();
        filter.expr("displayName", Operator.greatOrEqual, new ExprEntry("1", Demo.class));
        Assert.assertEquals("`display_name` >= 1", filter.getQueryData().getWhere());
        filter = new DynamicQueryFilter();
        //注意，这里仅是个单元测试，不建议这么用
        filter.expr("length(`displayName`)", Operator.greatOrEqual, new ExprEntry("unix_timestam()", Demo.class));
        Assert.assertEquals("length(`display_name`) >= unix_timestam()", filter.getQueryData().getWhere());
        filter = new DynamicQueryFilter();
        filter.expr("length(@displayName@)", Operator.greatOrEqual, new ExprEntry("unix_timestam()", Demo.class));
        Assert.assertEquals("length(`display_name`) >= unix_timestam()", filter.getQueryData().getWhere());
    }
    
    @Test
    public void sortTest() {
        DynamicQueryFilter filter = new DynamicQueryFilter();
        filter.orderBy("a", true)
              .orderBy("b", false);
        assertEquals("order by `a` desc, `b` asc", filter.getQueryData().get("SortSQL"));
    }

}
