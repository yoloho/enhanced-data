package com.yoloho.data.cache.redis.support;

/**
 * 存储zscan的返回结果
 * 
 * @author jason<jason@dayima.com> @ Mar 18, 2019
 *
 */
public class ZItem {
    private byte[] value;
    private double score;

    public ZItem(byte[] value, double score) {
        this.value = value;
        this.score = score;
    }

    public byte[] getValue() {
        return value;
    }

    public double getScore() {
        return score;
    }
}
