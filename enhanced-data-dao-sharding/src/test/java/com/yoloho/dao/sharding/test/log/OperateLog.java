package com.yoloho.dao.sharding.test.log;

import java.util.Date;

import com.yoloho.dao.api.Enhanced;
import com.yoloho.dao.api.EnhancedType;
import com.yoloho.dao.api.PrimaryKey;
import com.yoloho.dao.sharding.annotation.Sharded;
import com.yoloho.dao.sharding.annotation.ShardedFactor;
import com.yoloho.dao.sharding.strategy.ShardingStrategy;

@Enhanced(type=EnhancedType.SHARDED)
@Sharded(table="sys_operate_log", strategy=ShardingStrategy.TIME_YEAR)
public class OperateLog implements java.io.Serializable {

	private static final long serialVersionUID = 6258169831464103447L;

	@PrimaryKey
	private Long id;
	private String name;
	private String summary;
	private String appCode;
	private int logType;
	private long operator;
	private String operatorName;
	private long operateTime;
	private String logInfo;
	private String remark;
	private String reserve1;
	private String reserve2;
	private String reserve3;
	@ShardedFactor
	private Date created;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public int getLogType() {
		return logType;
	}

	public void setLogType(int logType) {
		this.logType = logType;
	}

	public long getOperator() {
		return operator;
	}

	public void setOperator(long operator) {
		this.operator = operator;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public long getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(long operateTime) {
		this.operateTime = operateTime;
	}

	public String getLogInfo() {
		return logInfo;
	}
	public void setLogInfo(String logInfo) {
		this.logInfo = logInfo;
	}

	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getReserve1() {
		return reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	public String getReserve2() {
		return reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}

	public String getReserve3() {
		return reserve3;
	}

	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}

}