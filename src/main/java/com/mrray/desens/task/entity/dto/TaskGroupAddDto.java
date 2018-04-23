package com.mrray.desens.task.entity.dto;

import com.mrray.desens.task.entity.domain.TaskGroup;
import org.springframework.beans.BeanUtils;

public class TaskGroupAddDto {

    private String groupName;

    public TaskGroup convert2TaskGroup() {
        TaskGroup group = new TaskGroup();

        // copy basic property
        BeanUtils.copyProperties(this, group);

        return group;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
