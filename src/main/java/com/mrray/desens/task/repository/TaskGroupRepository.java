package com.mrray.desens.task.repository;

import com.mrray.desens.task.entity.domain.TaskGroup;

public interface TaskGroupRepository extends BaseRepository<TaskGroup> {


    TaskGroup findByGroupName(String groupName);
}
