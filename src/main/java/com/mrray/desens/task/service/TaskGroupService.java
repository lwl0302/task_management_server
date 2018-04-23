package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.dto.TaskGroupAddDto;
import com.mrray.desens.task.entity.dto.TaskGroupPageDto;
import com.mrray.desens.task.entity.dto.TaskGroupUpdateDto;
import com.mrray.desens.task.entity.vo.RespBody;

public interface TaskGroupService {

    RespBody findAll(TaskGroupPageDto dto);

    RespBody findAll(String groupName);

    RespBody save(TaskGroupAddDto dto);

    RespBody save(TaskGroupUpdateDto dto);

}
