package com.mrray.desens.task.entity.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_task_group")
public class TaskGroup extends SuperEntity {

    @Column(unique = true, nullable = false)
    private String groupName;

    @OneToMany(targetEntity = Task.class, mappedBy = "group")
    private List<Task> tasks = new ArrayList<>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
