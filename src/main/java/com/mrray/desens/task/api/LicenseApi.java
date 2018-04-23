package com.mrray.desens.task.api;

import com.mrray.desens.task.entity.dto.AppLicenseDto;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/license")
public class LicenseApi {

    private final LicenseService licenseService;

    @Autowired
    public LicenseApi(LicenseService licenseService) {
        this.licenseService = licenseService;
    }


    /**
     * 检查程序的激活状态
     * @return
     */
    @GetMapping("/")
    public ResponseEntity checkActiveStatus() {

        RespBody body = licenseService.checkActiveStatus();

        return new ResponseEntity<>(body, body.getStatus());

    }

    @GetMapping("/serial")
    public ResponseEntity getAppSerial() {

        RespBody body = licenseService.getAppSerial();

        return new ResponseEntity<>(body, body.getStatus());
    }

    @PostMapping("/")
    public ResponseEntity activeApp(@RequestBody @Valid AppLicenseDto dto) {

        RespBody body = licenseService.activeApp(dto);

        return new ResponseEntity<>(body, body.getStatus());
    }

}
