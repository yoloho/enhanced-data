package com.yoloho.enhanced.data.dao.api.filter;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.yoloho.enhanced.common.util.StringUtil;


public class SortCommandImpl implements QueryCommand{
    private static final long serialVersionUID = 1L;
    private String sortName;
	private boolean isDesc;

	public SortCommandImpl(String sortName, boolean isDesc) {
		this.sortName = sortName;
		this.isDesc = isDesc;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public boolean getIsDesc() {
		return isDesc;
	}

	public void setIsDesc(boolean isDesc) {
		this.isDesc = isDesc;
	}

	public int hashCode() {
        return (new HashCodeBuilder(0xfb187f93, 0xd642e94b))
                .append(sortName)
                .append(isDesc ? "desc" : "asc")
                .toHashCode();
	}
	
	public String getPartSql() {
        return new StringBuilder(" order by ")
                .append(StringUtil.toUnderline(sortName))
                .append(" ")
                .append(isDesc ? "desc" : "asc")
                .toString();
	}

}
