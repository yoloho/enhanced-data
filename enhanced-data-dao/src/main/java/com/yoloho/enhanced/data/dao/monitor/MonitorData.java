package com.yoloho.enhanced.data.dao.monitor;

/**
 * @author jason
 *
 */
public class MonitorData {
    String entrypoint;
    private String dbHost;
    private int dbPort;
    private String db;
    private String metric;
    private int value;

    public MonitorData(String entrypoint, String host, int port, String db, String metric, int value) {
        this.entrypoint = entrypoint;
        this.metric = metric;
        this.value = value;
        this.dbHost = host;
        this.dbPort = port;
        this.db = db;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public String getDbHost() {
        return dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDb() {
        return db;
    }

    public String getMetric() {
        return metric;
    }

    public int getValue() {
        return value;
    }
}