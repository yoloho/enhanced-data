package com.yoloho.data.dao.monitor;

import java.util.List;

public interface MonitorCallback {
    void receive(List<MonitorData> dataList, int intervalInSeconds);
}