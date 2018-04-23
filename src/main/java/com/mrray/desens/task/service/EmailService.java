package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.dto.EmailSaveDto;
import com.mrray.desens.task.entity.vo.RespBody;

public interface EmailService {

    RespBody save(EmailSaveDto dto);

    RespBody find();

    RespBody updateStatus();

}
