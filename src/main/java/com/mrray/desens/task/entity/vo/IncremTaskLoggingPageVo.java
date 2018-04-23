package com.mrray.desens.task.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class IncremTaskLoggingPageVo {

    /**
     * 任务的ID
     */
    private Long taskId;

    /**
     * 任务的UUID
     */
    private String taskUuid;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startAt;

    /**
     * 自动还是手动   true 自动  false 手动
     */
    private boolean auto;

    /**
     * 成功还是失败
     */
    private boolean success;

    /**
     * 耗费的时间
     */
    private String spendTime;

    /**
     * 处理的数据条数
     */
    private Long procData;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskUuid() {
        return taskUuid;
    }

    public void setTaskUuid(String taskUuid) {
        this.taskUuid = taskUuid;
    }

    public Date getStartAt() {
        return startAt;
    }

    public void setStartAt(Date startAt) {
        this.startAt = startAt;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(String spendTime) {
        this.spendTime = spendTime;
    }

    public Long getProcData() {
        return procData;
    }

    public void setProcData(Long procData) {
        this.procData = procData;
    }
}
