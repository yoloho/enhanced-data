package com.yoloho.enhanced.data.dao.monitor;

import java.util.List;

public interface MonitorCallback {
    void receive(List<MonitorData> dataList, int intervalInSeconds);
}