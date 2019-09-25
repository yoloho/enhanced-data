package com.yoloho.enhanced.data.cache.lock;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.yoloho.enhanced.common.util.RandomUtil;
import com.yoloho.enhanced.data.cache.redis.api.RedisService;

/**
 * 基于redis的一个分布式锁实现<br>
 * 用法： 直接new，注意构造参数需要尽量做到唯一<br>
 * 注意：<br>
 * * 提供了unlock功能，但需要做到实例级别谁锁谁解(通过uuid实现)<br>
 * * lock/tryToLock默认哪怕同一实例的重复执行，后续执行也会失败或阻塞
 * 
 * @author jason<jason@dayima.com> @ Jan 3, 2019
 *
 */
public class DistributedLock<T> {
    private static final Logger logger = LoggerFactory.getLogger(DistributedLock.class.getSimpleName());
    
    /**
     * 锁实现接口
     * 
     * @author jason<jason@dayima.com> @ Mar 13, 2019
     *
     */
    public static interface LockSupport {
        /**
         * Ensure the lock data in storage won't disappear during the period and update the content 
         * 保持锁至少从现在起keepInSeconds秒数不过期，并更新锁内容
         * 
         * @param key
         * @param value
         * @param keepInSeconds
         */
        void keep(String key, String value, int keepInSeconds);
        /**
         * 检查指定的锁是否存在
         * 
         * @param key
         * @return
         */
        boolean exists(String key);
        /**
         * 获取锁内容
         * 
         * @param key
         * @return
         */
        String get(String key);
        /**
         * 当锁不存在时，创建锁
         * 
         * @param key
         * @param value
         * @param expireInSeconds
         * @return true for 设置成功
         */
        boolean setIfAbsent(String key, String value, int expireInSeconds);
        /**
         * 删除锁
         * 
         * @param key
         */
        void delete(String key);
    }
    
    private String uuid;
    private String namespace;
    private LockSupport lockSupport;
    private int expireInSeconds;
    
    /**
     * @param redisService 使用的redis实例
     * @param namespace 命名空间，至少5字符，尽量全局唯一
     * @param expireInSeconds 单次加锁后的锁过期秒数，如果想保持锁可以在有效期间内调用保持方法
     */
    public DistributedLock(final RedisService redisService, String namespace, final int expireInSeconds) {
        Preconditions.checkNotNull(namespace, "锁命名空间不能为空");
        Preconditions.checkNotNull(redisService, "redis服务不能为空");
        Preconditions.checkArgument(namespace.length() >= 5, "命名空间至少为5个字符");
        this.namespace = namespace;
        this.lockSupport = new LockSupport() {
            
            @Override
            public boolean setIfAbsent(String key, String value, int expireInSeconds) {
                return redisService.setIfAbsent(key, value, expireInSeconds);
            }
            
            @Override
            public void keep(String key, String value, int keepInSeconds) {
                redisService.set(key, value, keepInSeconds);
            }
            
            @Override
            public String get(String key) {
                return redisService.get(key);
            }
            
            @Override
            public boolean exists(String key) {
                return redisService.exists(key);
            }
            
            @Override
            public void delete(String key) {
                redisService.delete(key);
            }
        };
        this.expireInSeconds = expireInSeconds;
        this.uuid = RandomUtil.getRandomString(20);
    }
    
    /**
     * 提供了自定义实现的接口参数
     * 
     * @param lockSupport
     * @param namespace
     * @param expireInSeconds
     */
    public DistributedLock(LockSupport lockSupport, String namespace, final int expireInSeconds) {
        Preconditions.checkNotNull(namespace, "锁命名空间不能为空");
        Preconditions.checkNotNull(lockSupport, "锁实现对象不能为空");
        Preconditions.checkArgument(namespace.length() >= 5, "命名空间至少为5个字符");
        this.namespace = namespace;
        this.lockSupport = lockSupport;
        this.expireInSeconds = expireInSeconds;
        this.uuid = RandomUtil.getRandomString(20);
    }
    
    protected String lockKey(T key) {
        return new StringBuilder("lock::")
                .append(namespace)
                .append("::")
                .append(key)
                .toString();
    }
    
    /**
     * 锁保持
     * 
     * @param key
     * @param keepInSeconds 继续保持该锁的秒数
     */
    public void keepLock(T key) {
        String lockKey = lockKey(key);
        String value = lockSupport.get(lockKey);
        if (StringUtils.isEmpty(value)) {
            return;
        }
        int pos = value.indexOf('|');
        if (pos < 1) {
            return;
        }
        // uuid|timestamp
        String uuid = value.substring(0, pos);
        if (!StringUtils.equals(uuid, this.uuid)) {
            // uuid not match
            return;
        }
        value = value.substring(0, pos + 1) + System.currentTimeMillis();
        lockSupport.keep(lockKey, value, expireInSeconds);
    }
    
    /**
     * 尝试加锁，无论成功失败立即返回结果
     * 
     * @param key
     * @return
     */
    public boolean tryToLock(T key) {
        String lockKey = lockKey(key);
        // 这里貌似tmd有问题哦，key过期后的首次exists会返回true，get也会，第二次才消失
        if (lockSupport.exists(lockKey)) {
            // fail
            // double check
            String val = lockSupport.get(lockKey);
            if (StringUtils.isNotEmpty(val)) {
                val = lockSupport.get(lockKey);
                if (StringUtils.isNotEmpty(val)) {
                    int pos = val.indexOf('|');
                    if (pos > 0) {
                        long ts = NumberUtils.toLong(val.substring(pos + 1));
                        if (ts > 0 && System.currentTimeMillis() - ts < expireInSeconds * 1000) {
                            // not safe about the timestamp(distributed environment)
                            return false;
                        }
                    } else {
                        logger.warn("lock key logically expired");
                    }
                } else {
                    logger.warn("lock key consistent error");
                }
            }
            lockSupport.delete(lockKey);
        }
        // try to lock
        String val = String.format("%s|%d", this.uuid, System.currentTimeMillis());
        if (lockSupport.setIfAbsent(lockKey, val, expireInSeconds)) {
            // succ
            return true;
        }
        return false;
    }
    
    /**
     * 解锁需要使用同一实例来解锁，否则失败
     */
    public boolean unlock(T key) {
        String lockKey = lockKey(key);
        String val = lockSupport.get(lockKey);
        if (StringUtils.isEmpty(val) || val.indexOf('|') == -1) {
            return false;
        }
        String id = val.substring(0, val.indexOf('|'));
        if (StringUtils.equals(this.uuid, id)) {
            //unlock
            lockSupport.delete(lockKey);
            return true;
        }
        return false;
    }
    
    /**
     * 带等待时间的加锁，锁失败抛异常
     * 
     * @param key
     * @param waitTime
     * @param unit
     * @return
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public Closeable lock(T key, int waitTime, TimeUnit unit) throws TimeoutException, InterruptedException {
        long millis = unit.toMillis(waitTime);
        while (true) {
            boolean locked = tryToLock(key);
            if (locked) {
                //succ
                return new Closeable() {
                    @Override
                    public void close() throws IOException {
                        unlock(key);
                    }
                };
            }
            TimeUnit.MILLISECONDS.sleep(10);
            millis -= 10;
            if (millis < 1) {
                //fail
                throw new TimeoutException("lock failed");
            }
        }
    }
}
