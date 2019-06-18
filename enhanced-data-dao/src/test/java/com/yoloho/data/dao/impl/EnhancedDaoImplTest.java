package com.yoloho.data.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yoloho.common.util.Logging;
import com.yoloho.data.dao.api.IgnoreKey;
import com.yoloho.data.dao.api.PrimaryKey;
import com.yoloho.data.dao.api.UnionPrimaryKey;
import com.yoloho.data.dao.api.UpdateEntry;
import com.yoloho.data.dao.api.filter.DynamicQueryFilter;

/**
 * 简单写的单元测试，直接连了个测试库
 * 
 * @author jason<jason@dayima.com> @ May 29, 2018
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:context.xml")
public class EnhancedDaoImplTest {
    private final static Logger logger = LoggerFactory.getLogger(EnhancedDaoImplTest.class.getSimpleName());
    
    @Before
    public void init() {
        Logging.initLogging(true, false);
    }
    
    public static class UnitTestUserSignature {
        @PrimaryKey(autoIncrement = true)
        private Integer id;
        private int uid;
        private String content;
        private int size;
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public int getUid() {
            return uid;
        }
        public void setUid(int uid) {
            this.uid = uid;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public int getSize() {
            return size;
        }
        public void setSize(int size) {
            this.size = size;
        }
    }
    
    public static class UnitTestUserWired {
        @PrimaryKey(autoIncrement = true)
        private int id;
        @PrimaryKey
        private int uid;
        private String content;
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public int getUid() {
            return uid;
        }
        public void setUid(int uid) {
            this.uid = uid;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    public static class UnitTestUserCalendar {
        @PrimaryKey
        private int uid;
        @PrimaryKey
        private int dateline;
        @PrimaryKey
        private String eventKey;
        private String content;
        public int getUid() {
            return uid;
        }
        public void setUid(int uid) {
            this.uid = uid;
        }
        public int getDateline() {
            return dateline;
        }
        public void setDateline(int dateline) {
            this.dateline = dateline;
        }
        public String getEventKey() {
            return eventKey;
        }
        public void setEventKey(String eventKey) {
            this.eventKey = eventKey;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    public static class UnitTestUserLog {
        @PrimaryKey(autoIncrement = true)
        private int id;
        private int uid;
        private String memo;
        private int dateline;
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public int getUid() {
            return uid;
        }
        public void setUid(int uid) {
            this.uid = uid;
        }
        public String getMemo() {
            return memo;
        }
        public void setMemo(String memo) {
            this.memo = memo;
        }
        public int getDateline() {
            return dateline;
        }
        public void setDateline(int dateline) {
            this.dateline = dateline;
        }
    }
    
    public static class UnitTestUserMapping {
        @PrimaryKey
        private int uid;
        private int otherId;
        private String memo;
        private int dateline;
        public int getUid() {
            return uid;
        }
        public void setUid(int uid) {
            this.uid = uid;
        }
        public int getOtherId() {
            return otherId;
        }
        public void setOtherId(int otherId) {
            this.otherId = otherId;
        }
        public String getMemo() {
            return memo;
        }
        public void setMemo(String memo) {
            this.memo = memo;
        }
        public int getDateline() {
            return dateline;
        }
        public void setDateline(int dateline) {
            this.dateline = dateline;
        }
    }
    public static class UnitTestUser {
        @PrimaryKey
        private int id;
        private int groupId;
        private String nick;
        private String name;
        private String password;
        private int status;
        private String uids;
        @IgnoreKey
        private String ignoreKye = "test";
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public int getGroupId() {
            return groupId;
        }
        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }
        public String getNick() {
            return nick;
        }
        public void setNick(String nick) {
            this.nick = nick;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public int getStatus() {
            return status;
        }
        public void setStatus(int status) {
            this.status = status;
        }
        public String getUids() {
            return uids;
        }
        public void setUids(String uids) {
            this.uids = uids;
        }
        public String getIgnoreKye() {
            return ignoreKye;
        }
    }
    
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    
    /**
     * 简单主键
     */
    @Test
    public void getFindTest() {
        EnhancedDaoImpl<UnitTestUser, Integer> dao = new EnhancedDaoImpl<UnitTestUser, Integer>();
        dao.setSqlSessionFactory(sqlSessionFactory);
        dao.setTableName(UnitTestUser.class);
        /**
         * get
         */
        {
            Assert.assertNull(dao.get(0));
        }
        {
            UnitTestUser user = dao.get(1);
            Assert.assertEquals(1, user.getId());
            Assert.assertEquals("test", user.getIgnoreKye());
        }
        {
            List<UnitTestUser> list = dao.find(1, 2, 3, 4, 5, 6, 16);
            Assert.assertNotNull(list);
            Assert.assertTrue(list.size() > 1);
            Assert.assertTrue(list.get(0).getId() > 0);
        }
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.equalPair("id", 1);
            UnitTestUser user = dao.get(filter.getQueryData());
            Assert.assertEquals(1, user.getId());
            Assert.assertEquals("test", user.getIgnoreKye());
        }
        /**
         * find
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            List<UnitTestUser> list = dao.find(filter.getQueryData());
            Assert.assertNotNull(list);
            Assert.assertTrue(list.size() > 1);
            Assert.assertTrue(list.get(0).getId() > 0);
        }
        /**
         * count
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            int count = dao.count(filter.getQueryData());
            Assert.assertTrue(count > 0);
        }
        /**
         * sum
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            int sum = dao.sum("id", filter.getQueryData());
            Assert.assertTrue(sum > 0);
        }
    }
    /**
     * 读删写测试
     */
    @Test
    public void insertRemoveTest() {
        EnhancedDaoImpl<UnitTestUserMapping, Integer> dao = new EnhancedDaoImpl<UnitTestUserMapping, Integer>();
        dao.setSqlSessionFactory(sqlSessionFactory);
        dao.setTableName(UnitTestUserMapping.class);
        dao.setBatchSize(3);
        List<Integer> idList = Lists.newArrayList();
        /**
         * 不带自增
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            logger.info("remove {} records", dao.remove(filter.getQueryData()));
        }
        {
            UnitTestUserMapping bean = new UnitTestUserMapping();
            bean.setUid(1);
            bean.setOtherId(101);
            bean.setMemo("user 1 contains ` special chars");
            bean.setDateline((int)(System.currentTimeMillis() / 1000));
            Assert.assertEquals(1, dao.insert(bean));
            idList.add(1);
        }
        {
            List<UnitTestUserMapping> list = Lists.newArrayList();
            for (int i = 2; i < 19; i++) {
                UnitTestUserMapping bean = new UnitTestUserMapping();
                bean.setUid(i);
                bean.setOtherId(100 + i);
                bean.setMemo(String.format("user %d contains `&*^%%(^&^*%%@'\" special chars", i));
                bean.setDateline((int)(System.currentTimeMillis() / 1000) + i);
                list.add(bean);
                idList.add(i);
            }
            Assert.assertEquals(17, dao.insert(list));
        }
        {
            //update
            UnitTestUserMapping mapping = dao.get(10);
            Assert.assertNotNull(mapping);
            Assert.assertEquals(10, mapping.getUid());
            mapping.setMemo("new val");
            Assert.assertEquals(1, dao.update(mapping));
            mapping = dao.get(10);
            Assert.assertNotNull(mapping);
            Assert.assertEquals(10, mapping.getUid());
            Assert.assertEquals("new val", mapping.getMemo());
            //inc
            int oldDateline = mapping.getDateline();
            Map<String, UpdateEntry> data = Maps.newConcurrentMap();
            data.put("dateline", new UpdateEntry().increse(2));
            Assert.assertEquals(1, dao.update(data, new DynamicQueryFilter()
                    .equalPair("uid", 10)
                    .getQueryData()));
            mapping = dao.get(10);
            Assert.assertNotNull(mapping);
            Assert.assertEquals(10, mapping.getUid());
            Assert.assertEquals(oldDateline + 2, mapping.getDateline());
            Assert.assertEquals("new val", mapping.getMemo());
        }
        {
            //update batch
            String tail = " update append new val";
            List<UnitTestUserMapping> mappingList = dao.find(4, 5, 6);
            Assert.assertNotNull(mappingList);
            Assert.assertEquals(3, mappingList.size());
            for (UnitTestUserMapping unitTestUserMapping : mappingList) {
                unitTestUserMapping.setMemo(unitTestUserMapping.getMemo() + tail);
            }
            Assert.assertEquals(3, dao.update(mappingList));
            mappingList = dao.find(4, 5, 6);
            Assert.assertNotNull(mappingList);
            Assert.assertEquals(3, mappingList.size());
            Assert.assertTrue(mappingList.get(0).getMemo().contains(tail));
        }
        {
            // 试验性功能，条件批量修改
            String newVal = "batch update!!!!%";
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.greatOrEqual("otherId", 113);
            filter.lessThan("otherId", 117);
            Map<String, UpdateEntry> data = Maps.newHashMap();
            UpdateEntry entryMemo = new UpdateEntry();
            entryMemo.setValue(newVal);
            data.put("memo", entryMemo);
            UpdateEntry entryDateline = new UpdateEntry();
            entryDateline.setValue("@dateline@ - @dateline@ - 100000000");
            entryDateline.setPlain(true);
            data.put("dateline", entryDateline);
            UpdateEntry otherIdEntry = new UpdateEntry();
            otherIdEntry.setPlain(true);
            otherIdEntry.setValue("@otherId@ + 100");
            data.put("otherId", otherIdEntry);
            Assert.assertEquals(4, dao.update(data, filter.getQueryData()));
            //check old
            List<UnitTestUserMapping> list = dao.find(filter.getQueryData());
            Assert.assertEquals(0, list.size());
            //check new
            filter = new DynamicQueryFilter();
            filter.greatOrEqual("otherId", 213);
            filter.lessThan("otherId", 217);
            list = dao.find(filter.getQueryData());
            Assert.assertEquals(4, list.size());
            for (UnitTestUserMapping unitTestUserMapping : list) {
                Assert.assertEquals(-100000000, unitTestUserMapping.getDateline());
                Assert.assertEquals(entryMemo.getValue(), unitTestUserMapping.getMemo());
            }
        }
        {
            //clean data
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.in("uid", idList);
            Assert.assertEquals(idList.size(), dao.remove(filter.getQueryData()));
            logger.info("remove {} records", idList.size());
        }
        /**
         * 带自增
         */
        EnhancedDaoImpl<UnitTestUserLog, Integer> daoLog = new EnhancedDaoImpl<UnitTestUserLog, Integer>();
        daoLog.setSqlSessionFactory(sqlSessionFactory);
        daoLog.setTableName(UnitTestUserLog.class);
        daoLog.setBatchSize(3);
        idList.clear();
        {
            UnitTestUserLog bean = new UnitTestUserLog();
            bean.setUid(10);
            bean.setMemo("user 10 contains ` special chars");
            bean.setDateline((int)(System.currentTimeMillis() / 1000));
            bean = daoLog.insertAndReturn(bean);
            Assert.assertEquals(10, bean.getUid());
            Assert.assertTrue(bean.getId() > 0);
            idList.add(bean.getId());
        }
        {
            List<UnitTestUserLog> list = Lists.newArrayList();
            for (int i = 100; i < 110; i++) {
                UnitTestUserLog bean = new UnitTestUserLog();
                bean.setUid(i);
                bean.setMemo(String.format("user %d contains `&*^%%(^&^*%%@'\" special chars", i));
                bean.setDateline((int)(System.currentTimeMillis() / 1000) + i);
                list.add(bean);
            }
            List<UnitTestUserLog> beans = daoLog.insertAndReturn(list);
            for (UnitTestUserLog unitTestUserMapping : beans) {
                Assert.assertTrue(unitTestUserMapping.getId() > 0);
                idList.add(unitTestUserMapping.getId());
            }
        }
        {
            //clean data
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.in("id", idList);
            Assert.assertEquals(idList.size(), daoLog.remove(filter.getQueryData()));
            logger.info("remove {} records", idList.size());
        }
        /**
         * 联合主键测试
         */
        EnhancedDaoImpl<UnitTestUserCalendar, UnionPrimaryKey> daoCalendar = new EnhancedDaoImpl<>();
        daoCalendar.setSqlSessionFactory(sqlSessionFactory);
        daoCalendar.setTableName(UnitTestUserCalendar.class);
        {
            /**
             * 单插replace
             */
            UnitTestUserCalendar bean = new UnitTestUserCalendar();
            bean.setUid(1);
            bean.setDateline(20180101);
            bean.setEventKey("periodStart");
            bean.setContent("period has been started");
            Assert.assertTrue(1 <= daoCalendar.replace(bean));
            Assert.assertEquals(0, daoCalendar.insert(bean, true));
        }
        {
            /**
             * 多插replace
             */
            List<UnitTestUserCalendar> list = Lists.newArrayList();
            for (int i = 2; i < 10; i++) {
                UnitTestUserCalendar bean = new UnitTestUserCalendar();
                bean.setUid(i);
                bean.setDateline(20180101);
                bean.setEventKey("periodStart");
                bean.setContent("period has been started");
                list.add(bean);
            }
            Assert.assertTrue(list.size() <= daoCalendar.replace(list));
            Assert.assertEquals(0, daoCalendar.insert(list, true));
        }
        {
            /**
             * 单主键读+更新
             */
            UnionPrimaryKey key = new UnionPrimaryKey();
            key.put("uid", 1);
            key.put("dateline", 20180101);
            key.put("eventKey", "periodStart");
            UnitTestUserCalendar bean = daoCalendar.get(key);
            Assert.assertNotNull(bean);
            Assert.assertEquals(1, bean.getUid());
            Assert.assertEquals(20180101, bean.getDateline());
            Assert.assertEquals("periodStart", bean.getEventKey());
            Assert.assertEquals("period has been started", bean.getContent());
            bean.setContent("new val");
            Assert.assertEquals(1, daoCalendar.update(bean));
            bean = daoCalendar.get(key);
            Assert.assertNotNull(bean);
            Assert.assertEquals(1, bean.getUid());
            Assert.assertEquals("periodStart", bean.getEventKey());
            Assert.assertEquals("new val", bean.getContent());
        }
        {
            /**
             * 多主键读
             */
            List<UnionPrimaryKey> keys = Lists.newArrayList();
            for (int i = 2; i < 10; i++) {
                UnionPrimaryKey key = new UnionPrimaryKey();
                key.put("uid", i);
                key.put("dateline", 20180101);
                key.put("eventKey", "periodStart");
                keys.add(key);
            }
            List<UnitTestUserCalendar> list = daoCalendar.find(keys.toArray(new UnionPrimaryKey[] {}));
            Assert.assertEquals(8, list.size());
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setContent("new val2");
            }
            Assert.assertEquals(8, daoCalendar.update(list));
            list = daoCalendar.find(keys.toArray(new UnionPrimaryKey[] {}));
            Assert.assertEquals(8, list.size());
            for (int i = 0; i < list.size(); i++) {
                Assert.assertEquals("new val2", list.get(i).getContent());
            }
        }
        {
            /**
             * 单主键删除
             */
            UnionPrimaryKey key = new UnionPrimaryKey();
            key.put("uid", 1);
            key.put("dateline", 20180101);
            key.put("eventKey", "periodStart");
            Assert.assertEquals(1, daoCalendar.remove(key));
        }
        {
            /**
             * 多主键删除
             */
            List<UnionPrimaryKey> keys = Lists.newArrayList();
            for (int i = 2; i < 10; i++) {
                UnionPrimaryKey key = new UnionPrimaryKey();
                key.put("uid", i);
                key.put("dateline", 20180101);
                key.put("eventKey", "periodStart");
                keys.add(key);
            }
            Assert.assertEquals(8, daoCalendar.remove(keys.toArray(new UnionPrimaryKey[] {})));
        }
        /**
         * 联合主键＋自动增量
         */
        EnhancedDaoImpl<UnitTestUserWired, UnionPrimaryKey> daoWired = new EnhancedDaoImpl<UnitTestUserWired, UnionPrimaryKey>();
        daoWired.setSqlSessionFactory(sqlSessionFactory);
        daoWired.setTableName(UnitTestUserWired.class);
        daoWired.setBatchSize(3);
        {
            //单个手动设置
            UnitTestUserWired bean = new UnitTestUserWired();
            bean.setId(1);
            bean.setUid(10);
            bean.setContent("normal text");
            bean = daoWired.insertAndReturn(bean);
            Assert.assertEquals(1, bean.getId());
            Assert.assertEquals(10, bean.getUid());
            Assert.assertEquals("normal text", bean.getContent());
            UnionPrimaryKey key = new UnionPrimaryKey();
            key.put("id", 1);
            key.put("uid", 10);
            bean = daoWired.get(key);
            Assert.assertNotNull(bean);
            Assert.assertEquals(1, bean.getId());
            Assert.assertEquals(10, bean.getUid());
            Assert.assertEquals("normal text", bean.getContent());
            Assert.assertEquals(1, daoWired.remove(key));
        }
        {
            //单个获取自动增量
            UnitTestUserWired bean = new UnitTestUserWired();
            bean.setUid(11);
            bean.setContent("normal text1");
            bean = daoWired.insertAndReturn(bean);
            Assert.assertTrue(bean.getId() > 0);
            Assert.assertEquals(11, bean.getUid());
            Assert.assertEquals("normal text1", bean.getContent());
            UnionPrimaryKey key = new UnionPrimaryKey();
            key.put("id", bean.getId());
            key.put("uid", 11);
            bean = daoWired.get(key);
            Assert.assertNotNull(bean);
            Assert.assertTrue(bean.getId() > 0);
            Assert.assertEquals(11, bean.getUid());
            Assert.assertEquals("normal text1", bean.getContent());
            Assert.assertEquals(1, daoWired.remove(key));
        }
        {
            //多个手动设置
            List<UnionPrimaryKey> keys = Lists.newArrayList();
            List<UnitTestUserWired> beans = Lists.newArrayList();
            for (int i = 1; i < 10; i++) {
                UnitTestUserWired bean = new UnitTestUserWired();
                bean.setId(i);
                bean.setUid(10);
                bean.setContent("normal text " + i);
                beans.add(bean);
                UnionPrimaryKey key = new UnionPrimaryKey();
                key.put("id", i);
                key.put("uid", 10);
                keys.add(key);
            }
            beans = daoWired.insertAndReturn(beans);
            Assert.assertEquals(9, beans.size());
            UnitTestUserWired bean;
            for (int i = 0; i < beans.size(); i++) {
                bean = beans.get(i);
                Assert.assertTrue(bean.getId() > 0);
                Assert.assertEquals(10, bean.getUid());
                Assert.assertEquals("normal text " + bean.getId(), bean.getContent());
            }
            Assert.assertEquals(9, daoWired.remove(keys));
        }
        {
            //多个获取自动增量
            List<UnionPrimaryKey> keys = Lists.newArrayList();
            List<UnitTestUserWired> beans = Lists.newArrayList();
            for (int i = 1; i < 10; i++) {
                UnitTestUserWired bean = new UnitTestUserWired();
                bean.setUid(10);
                bean.setContent("normal text " + i);
                beans.add(bean);
                Assert.assertTrue(bean.getId() == 0);
            }
            beans = daoWired.insertAndReturn(beans);
            Assert.assertEquals(9, beans.size());
            UnitTestUserWired bean;
            for (int i = 0; i < beans.size(); i++) {
                bean = beans.get(i);
                Assert.assertTrue(bean.getId() > 0);
                Assert.assertEquals(10, bean.getUid());
                UnionPrimaryKey key = new UnionPrimaryKey();
                key.put("id", bean.getId());
                key.put("uid", bean.getUid());
                keys.add(key);
            }
            Assert.assertEquals(9, daoWired.remove(keys));
        }
        /**
         * 自动增量主键＋唯一索引约束＋insert ignore
         * 这种情况下，重复插入后，insertAndReturn返回的自动增量应该为默认值（如0）
         */
        EnhancedDaoImpl<UnitTestUserSignature, Integer> daoSignature = new EnhancedDaoImpl<UnitTestUserSignature, Integer>();
        daoSignature.setSqlSessionFactory(sqlSessionFactory);
        daoSignature.setTableName(UnitTestUserSignature.class);
        daoSignature.setBatchSize(3);
        {
            UnitTestUserSignature bean = new UnitTestUserSignature();
            bean.setUid(10);
            bean.setSize(123);
            bean.setId(0);
            bean.setContent("sign 1");
            bean = daoSignature.insertAndReturn(bean, true);
            Assert.assertTrue(bean.getId() > 0);
        }
        {
            UnitTestUserSignature bean = daoSignature.get("uid", 10);
            assertNotNull(bean);
            assertEquals(10, bean.getUid());
            assertEquals(123, bean.getSize());
            assertEquals("sign 1", bean.getContent());
        }
        {
            //重复插入
            UnitTestUserSignature bean = new UnitTestUserSignature();
            bean.setUid(10);
            bean.setSize(111);
            bean.setId(0);
            bean.setContent("sign 2");
            bean = daoSignature.insertAndReturn(bean, true);
            Assert.assertTrue(bean.getId() == 0);
        }
        {
            UnitTestUserSignature bean = daoSignature.get("uid", 10);
            assertNotNull(bean);
            assertEquals(10, bean.getUid());
            assertEquals(123, bean.getSize());
            assertEquals("sign 1", bean.getContent());
            
            // 更新
            bean.setSize(119);
            bean.setContent("sign 3");
            daoSignature.update(bean);
            bean = daoSignature.get("uid", 10);
            assertNotNull(bean);
            assertEquals(10, bean.getUid());
            assertEquals(119, bean.getSize());
            assertEquals("sign 3", bean.getContent());
            
            // 条件更新
            Map<String, UpdateEntry> dataMap = new HashMap<>(2);
            dataMap.put("content", new UpdateEntry("sign 4"));
            dataMap.put("size", new UpdateEntry().increse());
            daoSignature.update(dataMap, new DynamicQueryFilter()
                    .equalPair("uid", 10)
                    .getQueryData());
            bean = daoSignature.get("uid", 10);
            assertNotNull(bean);
            assertEquals(10, bean.getUid());
            assertEquals(120, bean.getSize());
            assertEquals("sign 4", bean.getContent());
            
            // 条件更新
            dataMap.put("content", new UpdateEntry("sign 5"));
            dataMap.put("size", new UpdateEntry("333"));
            daoSignature.update(dataMap, new DynamicQueryFilter()
                    .equalPair("uid", 10)
                    .getQueryData());
            bean = daoSignature.get("uid", 10);
            assertNotNull(bean);
            assertEquals(10, bean.getUid());
            assertEquals(333, bean.getSize());
            assertEquals("sign 5", bean.getContent());
        }
        {
            //清理
            daoSignature.remove(new DynamicQueryFilter().equalPair("uid", 10).getQueryData());
        }
    }
}
