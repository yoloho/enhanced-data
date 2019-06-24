package com.yoloho.enhanced.data.sharding.api;

import java.io.Serializable;

public class ShardingFactor implements Serializable{

	private static final long serialVersionUID = 2006654610843489015L;

	private String factorParam;
	private Object factorValue;
	
	public ShardingFactor(String factorParam, Object factorValue) {
		this.factorParam = factorParam;
		this.factorValue = factorValue;
	}
	
	public String getFactorParam() {
		return factorParam;
	}
	public void setFactorParam(String factorParam) {
		this.factorParam = factorParam;
	}
	
	public Object getFactorValue() {
		return factorValue;
	}
	public void setFactorValue(Object factorValue) {
		this.factorValue = factorValue;
	}

}