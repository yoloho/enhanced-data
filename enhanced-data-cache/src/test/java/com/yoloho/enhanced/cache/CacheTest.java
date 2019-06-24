package com.yoloho.enhanced.cache;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yoloho.enhanced.common.util.Logging;

/**
 * 目前缓存测试用例，直接改成了AspectJ模式，使用该模式可参考这套样例
 * aop.xml(可选), -javaagent, <enhanced-cache:init />, dependency
 * 
 * @author jason<jason@dayima.com> @ Mar 21, 2019
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:context.xml")
public class CacheTest {
    private static final Logger logger = LoggerFactory.getLogger(CacheTest.class.getSimpleName());
    
    @Autowired
    private DemoService demoService;
    
    @Before
    public void initLogging() {
        Logging.initLogging(true, false);
    }
    
    @Test
    public void cacheTest() {
        logger.info("同一参数的缓存测试，应仅最多实际调用一次");
        for (int i = 0; i < 10; i++) {
            Assert.assertNull(demoService.getValue());
        }
        logger.info("同一参数 + 变参数的缓存测试，应仅最多实际调用一次(变参数同一参数一次)");
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(2, demoService.getNewValue());
            for (int j = 0; j < 10; j++) {
                Assert.assertEquals(2 * i, demoService.getNewValue(i));
            }
        }
        logger.info("嵌套缓存测试，应仅最多实际调用一次");
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(2, demoService.compose());
        }
        logger.info("返回对象数组的缓存测试");
        for (int i = 0; i < 10; i++) {
            List<Item> list = demoService.array();
            Assert.assertTrue(list.size() > 0);
            Assert.assertEquals("test", list.get(0).getName());
            list.clear();
        }
        logger.info("变参数的缓存测试，应仅最多实际调用一次(变参数同一参数一次)");
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(2 * i, demoService.getNewValue(i));
        }
        logger.info("带更新的变参数的缓存测试，应每次循环均一次");
        for (int i = 0; i < 10; i++) {
            demoService.update(i);
            for (int j = 0; j < 10; j++) {
                Assert.assertEquals(2 * i, demoService.getNewValue(i));
            }
        }
    }
}
