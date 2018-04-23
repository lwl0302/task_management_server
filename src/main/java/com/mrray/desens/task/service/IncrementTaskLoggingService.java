package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.entity.domain.TaskStatus;
import com.mrray.desens.task.entity.dto.IncremTaskLoggingPageDto;
import com.mrray.desens.task.entity.vo.RespBody;

public interface IncrementTaskLoggingService {

    boolean save(Task task, Boolean auto, boolean hasError, String error, TaskStatus status);

    boolean update(Task task, boolean hasError, String error, TaskStatus status);

    RespBody page(IncremTaskLoggingPageDto dto);

}
