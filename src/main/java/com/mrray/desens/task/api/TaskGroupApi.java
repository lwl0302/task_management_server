package com.mrray.desens.task.api;


import com.mrray.desens.task.entity.dto.TaskGroupAddDto;
import com.mrray.desens.task.entity.dto.TaskGroupPageDto;
import com.mrray.desens.task.entity.dto.TaskGroupUpdateDto;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.service.TaskGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "taskGroup", description = "脱敏任务组管理", produces = MediaType.APPLICATION_JSON_VALUE)
@Controller
@RequestMapping("/api/v1/task/group")
public class TaskGroupApi {

    private final TaskGroupService taskGroupService;

    @Autowired
    public TaskGroupApi(TaskGroupService taskGroupService) {
        this.taskGroupService = taskGroupService;
    }


    @ApiOperation(value = "分页查询所有脱敏任务分组", notes = "分页查询所有脱敏任务分组")
    @GetMapping("/page")
    public ResponseEntity findAll(@Valid TaskGroupPageDto dto) {
        RespBody body = taskGroupService.findAll(dto);

        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "查询所有脱敏任务分组", notes = "查询所有脱敏任务分组")
    @GetMapping("/all")
    public ResponseEntity findAll(String groupName) {
        RespBody body = taskGroupService.findAll(groupName);

        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "创建单个脱敏任务分组", notes = "创建单个脱敏任务分组")
    @PostMapping("/one")
    public ResponseEntity save(@RequestBody @Valid TaskGroupAddDto dto) {
        RespBody body = taskGroupService.save(dto);

        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "修改单个脱敏任务分组", notes = "修改单个脱敏任务分组")
    @PutMapping("/one")
    public ResponseEntity save(@RequestBody @Valid TaskGroupUpdateDto dto) {
        RespBody body = taskGroupService.save(dto);

        return new ResponseEntity<>(body, body.getStatus());
    }

}
