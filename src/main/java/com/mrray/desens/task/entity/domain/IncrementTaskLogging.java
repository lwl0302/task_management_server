package com.mrray.desens.task.entity.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "t_increment_task_logging")
public class IncrementTaskLogging extends SuperEntity {

    /**
     * 关联的任务
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    /**
     * 自动还是手动
     */
    private boolean auto;

    /**
     * 进行到哪一步
     */
    private String step;

    /**
     * 成功还是失败
     */
    private boolean success = true;

    /**
     * 错误原因
     */
    private String error;

    /**
     * 处理的数据条数
     */
    private Long procData;

    /**
     * 完成时间
     */
    private Date finishAt;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getProcData() {
        return procData;
    }

    public void setProcData(Long procData) {
        this.procData = procData;
    }

    public Date getFinishAt() {
        return finishAt;
    }

    public void setFinishAt(Date finishAt) {
        this.finishAt = finishAt;
    }
}
