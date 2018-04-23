package com.mrray.desens.task.entity.dto;

/**
 * Created by ln on 2017/7/20.
 */
public class TargetInfoDto {
    private String id;

    private String tableName;

    private String fileType;

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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
