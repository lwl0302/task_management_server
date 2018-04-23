package com.mrray.desens.task.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mrray.desens.task.entity.domain.TaskGroup;
import org.springframework.beans.BeanUtils;

import java.util.Date;

public class TaskGroupPageVo {

    private String uuid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    private String groupName;

    public static TaskGroupPageVo convert(TaskGroup taskGroup) {

        TaskGroupPageVo vo = new TaskGroupPageVo();

        // copy basic property
        BeanUtils.copyProperties(taskGroup, vo);

        return vo;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
