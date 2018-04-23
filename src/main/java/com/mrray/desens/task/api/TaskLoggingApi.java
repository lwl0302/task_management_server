package com.mrray.desens.task.api;

import com.mrray.desens.task.entity.dto.IncremTaskLoggingPageDto;
import com.mrray.desens.task.entity.dto.TaskLoggingPageDto;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.service.IncrementTaskLoggingService;
import com.mrray.desens.task.service.TaskLoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/task/logging")
public class TaskLoggingApi {

    private final TaskLoggingService taskLoggingService;

    private final IncrementTaskLoggingService incrementTaskLoggingService;

    @Autowired
    public TaskLoggingApi(TaskLoggingService taskLoggingService, IncrementTaskLoggingService incrementTaskLoggingService) {
        this.taskLoggingService = taskLoggingService;
        this.incrementTaskLoggingService = incrementTaskLoggingService;
    }

    @GetMapping("/page")
    public ResponseEntity page(@Valid TaskLoggingPageDto dto) {

        RespBody body = taskLoggingService.page(dto);

        return new ResponseEntity<>(body, body.getStatus());
    }


    @GetMapping("/increment/page")
    public ResponseEntity page(@Valid IncremTaskLoggingPageDto dto) {

        RespBody body = incrementTaskLoggingService.page(dto);

        return new ResponseEntity<>(body, body.getStatus());

    }

}
