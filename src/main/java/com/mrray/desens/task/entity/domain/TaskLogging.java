package com.mrray.desens.task.entity.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Arthur on 2017/7/20.
 * 脱敏任务日志模型
 */

@Entity
@Table(name = "t_task_logging")
public class TaskLogging extends SuperEntity {
    // 任务的ID
    @Column(nullable = false, updatable = false, length = 10)
    private Long task;
    // 做的操作 新建（add），启动（start），暂停（pause），取消（cancel），完成（finish）, 失败（fail）
    @Column(nullable = false, updatable = false, length = 10)
    private String control;
    private String type;
    // 是否成功
    private boolean success;
    private Date time = new Date();
    private String sourceIp;
    private String sourceType;
    private String sourceDatabase;
    private int sourcePort;
    private String sourceName;
    private String targetIp;
    private String targetType;
    private String targetDatabase;
    private int targetPort;
    private String targetName;

    /**
     * 本次任务处理的数据条数
     */
    private long processData;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Long getTask() {
        return task;
    }

    public void setTask(Long task) {
        this.task = task;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getProcessData() {
        return processData;
    }

    public void setProcessData(long processData) {
        this.processData = processData;
    }
}
