package com.mrray.desens.task.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.AssertTrue;
import java.util.Date;

/**
 * Created by Arthur on 2017/7/20.
 */

@ApiModel(description = "任务列表查询参数模型")
public class TaskQueryDto extends PageQueryDto {

    @ApiModelProperty(value = "任务的UUID<br />UUID由小写字母和数字组成的长度为8的随机数")
    private Long uuid;

    @ApiModelProperty(value = "开始时间<br />格式为：yyyy-MM-dd HH:mm:ss，开始时间必须小于等于结束时间", example = "2017-07-31 13:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    @ApiModelProperty(value = "结束时间<br />格式为：yyyy-MM-dd HH:mm:ss，结束时间必须大于等于开始时间", example = "2017-07-31 15:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty(value = "模糊查询UUID和文件名")
    private String searchKey;
    // 任务状态
    @ApiModelProperty(value = "任务状态<br /> 为空表示所有状态<br /> -2表示失败的任务<br /> -1表示取消的任务<br /> 0表示新建的任务<br /> 1表示启动任务<br /> 2表示暂停的任务<br /> 3 表示成功的任务<br /> 4 表示等待执行的任务", allowableValues = "-2, -1, 0, 1, 2, 3", allowEmptyValue = true)
    private Integer status;
    // 策略状态
    @ApiModelProperty(value = "规则的状态<br /> 为空表示查询所有规则<br /> 0表示常用的规则<br /> 1表示自定义规则", allowableValues = "0, 1", allowEmptyValue = true)
    private Integer policyType;
    // 源类型
    @ApiModelProperty(value = "数据源类型<br /> 为空表示查询所有数据源的任务<br /> File表示文件上传的数据源<br /> " +
            "MySQL表示MySQL数据库<br /> Oracle表示Oracle数据库<br /> DB2表示DB2数据库<br /> IDS表示Informix Dynamic Server数据库<br /> " +
            "SQLServer表示SQLServer数据库<br /> Sybase表示Sybase数据库<br /> PostgreSQL表示PostgreSQL数据库<br />  GreenPlun表示GreenPlun数据库<br /> TeraData表示TeraData数据库",
            allowableValues = "File, MySQL, Oracle, DB2, IDS, SQLServer, Sybase, PostgreSQL, GreenPlun, TeraData",
            allowEmptyValue = true)
    private String sourceType;
    // 目标类型
    @ApiModelProperty(value = "目标源类型<br /> 为空表示查询所有数据源的任务<br /> File表示文件上传的数据源<br /> " +
            "MySQL表示MySQL数据库<br /> Oracle表示Oracle数据库<br /> DB2表示DB2数据库<br /> IDS表示Informix Dynamic Server数据库<br /> " +
            "SQLServer表示SQLServer数据库<br /> Sybase表示Sybase数据库<br /> PostgreSQL表示PostgreSQL数据库<br />  GreenPlun表示GreenPlun数据库<br /> TeraData表示TeraData数据库",
            allowableValues = "File, MySQL, Oracle, DB2, IDS, SQLServer, Sybase, PostgreSQL, GreenPlun, TeraData",
            allowEmptyValue = true)
    private String targetType;

    private String group;

    private String taskType;

    @AssertTrue(message = "beginTime must less than endTime")
    private boolean isValid() {
        if (this.beginTime != null && this.endTime != null) {
            return this.beginTime.getTime() <= this.endTime.getTime();
        }
        return true;
    }

    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPolicyType() {
        return policyType;
    }

    public void setPolicyType(Integer policyType) {
        this.policyType = policyType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
