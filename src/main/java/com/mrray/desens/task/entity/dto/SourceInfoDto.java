package com.mrray.desens.task.entity.dto;

import com.mrray.desens.task.entity.vo.BaseResourceInfoVo;

/**
 * Created by ln on 2017/7/20.
 */
public class SourceInfoDto extends BaseResourceInfoVo {

    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
