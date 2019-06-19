package com.yoloho.data.dao.monitor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yoloho.data.dao.util.IPUtils;

/**
 * 
 * @author jason
 *
 */
public class DruidMonitorInit extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DruidMonitorInit.class.getSimpleName());
    private static final Set<String> METRICS = Sets.newHashSet(
            "WaitThreadCount",
            "NotEmptyWaitCount",
            "NotEmptyWaitMillis",
            "PoolingCount",
            "PoolingPeak",
            "ActiveCount",
            "ActivePeak",
            "QueryTimeout",
            "TransactionQueryTimeout",
            "LoginTimeout",
            "LogicConnectCount",
            "LogicCloseCount",
            "LogicConnectErrorCount",
            "PhysicalConnectCount",
            "PhysicalCloseCount",
            "PhysicalConnectErrorCount",
            "ExecuteCount",
            "ErrorCount",
            "CommitCount",
            "RollbackCount",
            "PSCacheAccessCount",
            "PSCacheHitCount",
            "PSCacheMissCount",
            "StartTransactionCount",
            "ClobOpenCount",
            "BlobOpenCount"
            );
    
    private String entrypoint;
    private MonitorCallback callback;
    private boolean shutdown = false;
    private int intervalInSeconds = 60;
    
    private URI getURI(DruidDataSource dataSource) {
        try {
            String url = dataSource.getUrl();
            if (url.startsWith("jdbc:")) {
                url = url.substring(5);
            }
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    private List<MonitorData> getMonitorData(DruidDataSource dataSource) {
        List<MonitorData> result = Lists.newArrayList();
        Map<String, Object> stat = dataSource.getStatData();
        URI uri = getURI(dataSource);
        if (uri == null) return Collections.emptyList();
        for (Entry<String, Object> entry : stat.entrySet()) {
            if (METRICS.contains(entry.getKey())) {
                result.add(new MonitorData(
                        this.entrypoint, 
                        uri.getHost(), 
                        uri.getPort(), 
                        StringUtils.strip(uri.getPath(), "/"), 
                        entry.getKey(), 
                        NumberUtils.toInt(entry.getValue().toString())));
            }
        }
        return result;
    }
    
    public DruidMonitorInit(String projectName, MonitorCallback callback, int intervalInSeconds) {
        this.callback = callback;
        this.entrypoint = "druid." + projectName + "." + IPUtils.getLocalHost();
        this.intervalInSeconds = intervalInSeconds;
        setName("DruidMonitorThread");
        setDaemon(true);
    }
    
    @PostConstruct
    public void init() {
        start();
    }
    
    @PreDestroy
    public void deinit() {
        shutdown = true;
    }
    
    private void delay() {
        int counter = 60;
        while (counter -- > 0 && !shutdown) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
    
    private void monitor() {
        List<MonitorData> dataList = Lists.newArrayList();
        for (DruidDataSource druidDataSource : DruidDataSourceStatManager.getDruidDataSourceInstances()) {
            dataList.addAll(getMonitorData(druidDataSource));
        }
        if (dataList.size() > 0) {
            this.callback.receive(dataList, this.intervalInSeconds);
        }
    }
    
    @Override
    public void run() {
        while (!shutdown) {
            int noSourceCount = 0;
            if (DruidDataSourceStatManager.getDruidDataSourceInstances().size() == 0) {
                noSourceCount ++;
            }
            if (noSourceCount > 10) {
                // no datasource, exit
                break;
            }
            monitor();
            delay();
        }
    }
    
    /**
     * Initialize the druid pool monitor
     * 
     * @param registry
     * @param projectName
     * @param beanName bean name which implementing 
     */
    public static void init(BeanDefinitionRegistry registry, String projectName, String beanName, int intervalInSeconds) {
        if (StringUtils.isEmpty(projectName)) {
            return;
        }
        // druid monitor
        String className = "com.alibaba.druid.stat.DruidDataSourceStatManager";
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz != null) {
                // init default collector when beanName is empty
                if (StringUtils.isEmpty(beanName)) {
                    beanName = DefaultMonitorCallback.class.getName();
                    registry.registerBeanDefinition(beanName, BeanDefinitionBuilder
                            .genericBeanDefinition(DefaultMonitorCallback.class)
                            .getBeanDefinition());
                }
                //init zookeeper datasource
                BeanDefinitionBuilder druidMonitorInitBuilder = BeanDefinitionBuilder.genericBeanDefinition(DruidMonitorInit.class);
                druidMonitorInitBuilder.addConstructorArgValue(projectName);
                druidMonitorInitBuilder.addConstructorArgReference(beanName);
                druidMonitorInitBuilder.addConstructorArgValue(intervalInSeconds);
                druidMonitorInitBuilder.setLazyInit(false);
                registry.registerBeanDefinition("druidMonitorInitBuilder", druidMonitorInitBuilder.getBeanDefinition());
                logger.info("Enable monitor feature for druid pool");
            }
        } catch (Exception e) {
        }
    }

}
