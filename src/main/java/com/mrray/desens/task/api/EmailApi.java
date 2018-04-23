package com.mrray.desens.task.api;

import com.mrray.desens.task.entity.dto.EmailSaveDto;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.service.EmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(value = "taskGroup", description = "邮箱提醒设置", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/api/v1/email")
public class EmailApi {

    private final EmailService emailService;

    @Autowired
    public EmailApi(EmailService emailService) {
        this.emailService = emailService;
    }

    @ApiOperation(value = "添加邮箱", notes = "添加邮箱")
    @PostMapping("/one")
    public ResponseEntity save(@RequestBody EmailSaveDto dto) {

        RespBody body = emailService.save(dto);

        return new ResponseEntity<>(body, body.getStatus());

    }

    @GetMapping("/one")
    public ResponseEntity find() {

        RespBody body = emailService.find();

        return new ResponseEntity<>(body, body.getStatus());

    }

    @PutMapping("/one/status")
    public ResponseEntity updateStatus() {

        RespBody body = emailService.updateStatus();

        return new ResponseEntity<>(body, body.getStatus());

    }


}
