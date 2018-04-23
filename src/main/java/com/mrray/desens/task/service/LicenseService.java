package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.dto.AppLicenseDto;
import com.mrray.desens.task.entity.vo.RespBody;

public interface LicenseService {

    RespBody checkActiveStatus();

    RespBody getAppSerial();

    RespBody activeApp(AppLicenseDto dto);

}
