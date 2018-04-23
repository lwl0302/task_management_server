package com.mrray.desens.task.entity.vo;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by ln on 2017/7/20.
 */
public class BaseResourceInfoVo {
    @NotBlank(message = "IP_NOT_NULL")
    private String ip;
    @NotNull(message = "PORT_ID_NOT_NULL")
    private Integer port;
    @NotBlank(message = "UEERNAME_NOT_NULL")
    private String username;
    @NotBlank(message = "PASSWORD_NOT_NULL")
    private String password;
    @NotBlank(message = "DATABASE_NAME_NOT_NULL")
    private String databaseName;
    @NotBlank(message = "TYPE_NOT_NULL")
    private String dbType;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
}
