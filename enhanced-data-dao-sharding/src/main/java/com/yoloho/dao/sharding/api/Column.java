package com.yoloho.dao.sharding.api;

public class Column {

    private String columnName;
    private String PropertyName;
    private boolean primary = false;
    private boolean autoIncrement = false;
    
    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public String getPropertyName() {
        return PropertyName;
    }
    public void setPropertyName(String propertyName) {
        PropertyName = propertyName;
    }
    
    public boolean isPrimary() {
        return primary;
    }
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
    
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

}