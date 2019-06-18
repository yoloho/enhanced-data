package com.yoloho.cache;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.yoloho.cache.annotation.EnableCache;
import com.yoloho.cache.annotation.EnableCacheBoolean;
import com.yoloho.cache.annotation.EnableCacheConfig;
import com.yoloho.cache.annotation.EnableCacheEvict;

@Service
@EnableCacheConfig(group = "newgroup", expire = 77, local = EnableCacheBoolean.ENABLE, remote = EnableCacheBoolean.ENABLE)
public class DemoServiceImpl implements DemoService {
    private static final Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);
    
    @Autowired
    private DemoService demoService;
    
    @Override
    @EnableCache(group = "other", remote = EnableCacheBoolean.ENABLE, local = EnableCacheBoolean.ENABLE, expire = 60)
    public String getValue() {
        logger.info("invoke getValue() -> null");
        return null;
    }
    
    @Override
    @EnableCache(expire = 44)
    public int getNewValue() {
        logger.info("invoke getNewValue()");
        return 2;
    }
    
    @Override
    @EnableCache(key = "'val_' + #n")
    public int getNewValue(int n) {
        logger.info("invoke getNewValue({})", n);
        return 2 * n;
    }
    
    @Override
    public int compose() {
        return demoService.getNewValue();
    }
    
    @EnableCache
    @Override
    public List<Item> array() {
        return Lists.newArrayList(new Item(), new Item(), new Item(), new Item());
    }
    
    @EnableCacheEvict(group = {"newgroup", "other"}, key = {"'val_' + #n"})
    @Override
    public void update(int n) {
        //nothing
        logger.info("update: {}", n);
    }
}
