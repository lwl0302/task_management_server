package com.mrray.desens.task.entity.dto;

import java.util.Date;

/**
 * Created by ln on 2017/7/20.
 */
public class ExtractDto {
    private String mainTaskId;
    private String id;
    private String tableName;
    private Long done;
    private Long rows;
    private String columnType;
    private Long intPlus;
    private Date datePlus;
    private String columnName;
    private boolean plus;
    private String fields;

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getMainTaskId() {
        return mainTaskId;
    }

    public void setMainTaskId(String mainTaskId) {
        this.mainTaskId = mainTaskId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getDone() {
        return done;
    }

    public void setDone(Long done) {
        this.done = done;
    }

    public Long getRows() {
        return rows;
    }

    public void setRows(Long rows) {
        this.rows = rows;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public Long getIntPlus() {
        return intPlus;
    }

    public void setIntPlus(Long intPlus) {
        this.intPlus = intPlus;
    }

    public Date getDatePlus() {
        return datePlus;
    }

    public void setDatePlus(Date datePlus) {
        this.datePlus = datePlus;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isPlus() {
        return plus;
    }

    public void setPlus(boolean plus) {
        this.plus = plus;
    }
}
