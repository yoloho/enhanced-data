package com.yoloho.data.dao.monitor;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.yoloho.common.util.HttpClientUtil;

/**
 * @author jason
 *
 */
class DefaultMonitorCallback implements MonitorCallback {
    @Value("${falcon.url}")
    String falconUrl;
    
    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(falconUrl)) {
            // fallback to property
            falconUrl = System.getProperty("falcon.url");
        }
        if (StringUtils.isEmpty(falconUrl)) {
            throw new RuntimeException("No falcon.url found");
        }
    }

    @Override
    public void receive(List<MonitorData> dataList, int intervalInSeconds) {
        
        HttpClientUtil.asyncPostRequestWithJson(falconUrl, 
                JSON.toJSONString(dataList.stream()
                        .map(d -> new FalconData(d, intervalInSeconds))
                        .collect(Collectors.toList())), 
                new HttpClientUtil.Callback() {
            
            @Override
            public void completed(HttpUriRequest request, String result) {
            }
        });
    }
    
}