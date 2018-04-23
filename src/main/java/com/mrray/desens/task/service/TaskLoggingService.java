package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.dto.TaskLoggingPageDto;
import com.mrray.desens.task.entity.vo.RespBody;

public interface TaskLoggingService {
    RespBody page(TaskLoggingPageDto dto);
}
