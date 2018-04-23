package com.mrray.desens.task.entity.vo;

public class TaskStatisticVo {

    /**
     * 总得任务条数
     */
    private int totalTasks = 0;

    /**
     * 删除的任务
     */
    private int removedTasks = 0;

    /**
     * 完成的任务条数
     */
    private int finishedTasks = 0;

    /**
     * 暂停的任务条数
     */
    private int pauseTasks = 0;

    /**
     * 取消的任务条数
     */
    private int cancelTasks = 0;

    /**
     * 失败的任务条数
     */
    private int failedTasks = 0;

    /**
     * 未启动的任务
     */
    private int notstartTasks;

    /**
     * 自动增量任务
     */
    private int autoIncrementTasks = 0;

    /**
     * 手动增量任务
     */
    private int handIncrementTasks = 0;

    /**
     * 普通任务
     */
    private int normalTasks = 0;

    /**
     * 总共处理条数
     */
    private long totalProcessData = 0;

    /**
     * 增量处理条数
     */
    private long incrementProcessData = 0;

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getRemovedTasks() {
        return removedTasks;
    }

    public void setRemovedTasks(int removedTasks) {
        this.removedTasks = removedTasks;
    }

    public int getFinishedTasks() {
        return finishedTasks;
    }

    public void setFinishedTasks(int finishedTasks) {
        this.finishedTasks = finishedTasks;
    }

    public int getPauseTasks() {
        return pauseTasks;
    }

    public void setPauseTasks(int pauseTasks) {
        this.pauseTasks = pauseTasks;
    }

    public int getCancelTasks() {
        return cancelTasks;
    }

    public void setCancelTasks(int cancelTasks) {
        this.cancelTasks = cancelTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    public int getNotstartTasks() {
        return notstartTasks;
    }

    public void setNotstartTasks(int notstartTasks) {
        this.notstartTasks = notstartTasks;
    }

    public int getAutoIncrementTasks() {
        return autoIncrementTasks;
    }

    public void setAutoIncrementTasks(int autoIncrementTasks) {
        this.autoIncrementTasks = autoIncrementTasks;
    }

    public int getHandIncrementTasks() {
        return handIncrementTasks;
    }

    public void setHandIncrementTasks(int handIncrementTasks) {
        this.handIncrementTasks = handIncrementTasks;
    }

    public int getNormalTasks() {
        return normalTasks;
    }

    public void setNormalTasks(int normalTasks) {
        this.normalTasks = normalTasks;
    }

    public long getTotalProcessData() {
        return totalProcessData;
    }

    public void setTotalProcessData(long totalProcessData) {
        this.totalProcessData = totalProcessData;
    }

    public long getIncrementProcessData() {
        return incrementProcessData;
    }

    public void setIncrementProcessData(long incrementProcessData) {
        this.incrementProcessData = incrementProcessData;
    }
}
