package com.mrray.desens.task.entity.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Arthur on 2017/7/19.
 * 脱敏任务模型
 */

@Entity
@Table(name = "t_task")
public class Task extends SuperEntity {
    // 脱敏源ID
    @Column(nullable = false)
    private String source;
    // 脱敏源文件
    @Column(nullable = false)
    private String sourceFile;
    // 源类型 文件（File），Mysql， Oracle
    @Column(nullable = false)
    private String sourceType;
    // 目标源ID
    @Column(nullable = false)
    private String target;
    // 脱敏后装载的目标文件
    @Column(nullable = false)
    private String targetFile;
    // 目标类型  文件（File），MySQL， Oracle
    @Column(nullable = false)
    private String targetType;
    // 脱敏策略
    @Column(nullable = false)
    private String policy;
    // 策略是自定义还是已有的 0 已有  1自定义的
    private int policyType = -1;
    // 策略的名称
    @Column(nullable = false)
    private String policyName;
    // status -2 任务失败 -1 取消任务  0 新建任务  1 启动任务   2 暂停任务  3 任务成功    进度是实时获取不能存
    // 0 创建成功   1 抽取成功 -1 抽取失败      2 扫描成功 -2 扫描失败      3 脱敏成功 -3 脱敏失败      4 装载成功 -4 装载失败

    //0 创建成功 1 正在执行 2 暂停 3 取消 4 成功 5 等待  -1 抽取失败 -2 脱敏失败 -3 装载失败 -4 服务器宕机异常
    private int status = 0;
    private boolean deleted = false;
    private String extract;
    private String loads;
    private String scan;
    private String desensitive;
    private Date start;
    private Long cost = 0L;

    private String columnType;
    private Long intPlus;
    @Column(columnDefinition = "datetime(6) NULL DEFAULT NULL")
    private Date datePlus;
    private String columnName;

    /**
     * 标识是否是增量任务
     */
    private boolean plus;
    private Long done = 1L;


    /**
     * 单次任务处理的数据条数
     */
    private long processData = 0L;

    private String cron;
    private String cycle;
    private int point;
    private int month;
    private int year;

    private String execTime;

    /**
     * 标识是否是自动任务
     */
    private boolean auto;

    private String sourceName;
    private String sourceIp;
    private String sourceDatabase;
    private int sourcePort;

    private String targetName;
    private String targetIp;
    private String targetDatabase;
    private int targetPort;

    private String error;

    @ManyToOne(targetEntity = TaskGroup.class, fetch = FetchType.LAZY)
    private TaskGroup group;

    private Date autoStartTime;

    private String fields;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getExecTime() {
        return execTime;
    }

    public void setExecTime(String execTime) {
        this.execTime = execTime;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
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

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
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

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getScan() {
        return scan;
    }

    public void setScan(String scan) {
        this.scan = scan;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Task delete() {
        this.deleted = true;
        return this;
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

    public Long getDone() {
        return done;
    }

    public void setDone(Long done) {
        this.done = done;
    }

    public long getProcessData() {
        return processData;
    }

    public void setProcessData(long processData) {
        this.processData = processData;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }


    public String getExtract() {
        return extract;
    }

    public void setExtract(String extract) {
        this.extract = extract;
    }

    public String getLoads() {
        return loads;
    }

    public void setLoads(String loads) {
        this.loads = loads;
    }

    public String getDesensitive() {
        return desensitive;
    }

    public void setDesensitive(String desensitive) {
        this.desensitive = desensitive;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public int getPolicyType() {
        return policyType;
    }

    public void setPolicyType(int policyType) {
        this.policyType = policyType;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public TaskGroup getGroup() {
        return group;
    }

    public void setGroup(TaskGroup group) {
        this.group = group;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Date getAutoStartTime() {
        return autoStartTime;
    }

    public void setAutoStartTime(Date autoStartTime) {
        this.autoStartTime = autoStartTime;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }
}
