package com.yoloho.data.dao.monitor;

/**
 * @author jason
 *
 */
class FalconData {
    private String endpoint;
    private String metric;
    private int step;
    private String tags;
    private String counterType;
    private int value;
    
    public FalconData(MonitorData d, int step) {
        setCounterType("GAUGE");
        setStep(step);
        setTags(new StringBuilder()
                .append("host=").append(d.getDbHost())
                .append(":").append(d.getDbPort())
                .append("&db=").append(d.getDb())
                .toString());
        setEndpoint(d.entrypoint);
        setMetric(d.getMetric());
        setValue(d.getValue());
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCounterType() {
        return counterType;
    }

    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
}