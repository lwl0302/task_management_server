package com.mrray.desens.task.api;

import com.mrray.desens.task.entity.dto.AutoDto;
import com.mrray.desens.task.entity.dto.TaskAddDto;
import com.mrray.desens.task.entity.dto.TaskQueryDto;
import com.mrray.desens.task.entity.dto.TaskStatisticDto;
import com.mrray.desens.task.entity.vo.ExtractRespVo;
import com.mrray.desens.task.entity.vo.LoadRespVo;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.entity.vo.RestResponseBody;
import com.mrray.desens.task.service.TaskService;
import com.mrray.desens.task.service.impl.TaskServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Created by Arthur on 2017/7/19.
 */

@Api(value = "task", description = "脱敏任务管理", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskApi {
    private final TaskService taskService;

    @Autowired
    public TaskApi(TaskService taskService) {
        this.taskService = taskService;
    }

    @ApiOperation(value = "查询任务列表", notes = "可以根据创建时间和状态等来查询任务列表", response = ResponseEntity.class)
    @GetMapping("")
    public ResponseEntity getTasks(@Valid TaskQueryDto dto) {

        RespBody body = taskService.getTasks(dto);

        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "首页信息", response = ResponseEntity.class)
    @GetMapping("/index")
    public ResponseEntity index() {

        RespBody body = taskService.index();

        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "新建任务", notes = "传入脱敏源、目标源和策略创建一个新的脱敏任务。")
    @PostMapping("")
    public ResponseEntity addTask(@RequestBody @Valid TaskAddDto dto) {

        RespBody body = taskService.addAndupdateTask(null, dto);

        return new ResponseEntity<>(body, body.getStatus());
    }


    @ApiOperation(value = "启动脱敏任务", notes = "启动脱敏任务")
    @PutMapping("/{uuid}/start")
    public ResponseEntity startTask(@PathVariable(name = "uuid") String uuid) {

        RespBody body = taskService.startTask(uuid);

        return new ResponseEntity<>(body, body.getStatus());
    }


    @ApiOperation(value = "暂停脱敏任务", notes = "暂停脱敏任务")
    @PutMapping("/{uuid}/pause")
    public ResponseEntity pauseTask(@PathVariable(name = "uuid") String uuid) {

        RespBody body = taskService.pauseTask(uuid);

        return new ResponseEntity<>(body, body.getStatus());
    }


    @ApiOperation(value = "取消脱敏任务", notes = "取消脱敏任务")
    @PutMapping("/{uuid}/cancel")
    public ResponseEntity cancelTask(@PathVariable(name = "uuid") String uuid) {

        RespBody body = taskService.cancelTask(uuid);

        return new ResponseEntity<>(body, body.getStatus());
    }


    @ApiOperation(value = "删除脱敏任务", notes = "删除脱敏任务")
    @DeleteMapping("/")
    public ResponseEntity delete(String[] uuids) {

        RespBody body = taskService.delete(uuids);

        return new ResponseEntity<>(body, body.getStatus());
    }


    @ApiOperation(value = "修改任务配置信息", notes = "修改指定任务的配置信息")
    @PutMapping("/{uuid}")
    public ResponseEntity updateTask(@PathVariable(name = "uuid") String uuid, @RequestBody @Valid TaskAddDto dto) {

        RespBody body = taskService.addAndupdateTask(uuid, dto);

        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "更新数据库")
    @PutMapping("/updateDB/{uuid}")
    public ResponseEntity updateDB(@PathVariable(name = "uuid") String uuid) {
        RespBody body = taskService.updateDB(uuid);
        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "设置增量")
    @PutMapping("/{uuid}/plus")
    public ResponseEntity plus(@PathVariable(name = "uuid") String uuid, String colunmName, String columnType) {
        RespBody body = taskService.plus(uuid, colunmName, columnType);
        return new ResponseEntity<>(body, body.getStatus());
    }

    @ApiOperation(value = "设置周期")
    @PutMapping("/{uuid}/auto")
    public ResponseEntity auto(@PathVariable(name = "uuid") String uuid, @RequestBody @Valid AutoDto autoDto) {
        RespBody body = taskService.auto(uuid, autoDto);
        return new ResponseEntity<>(body, body.getStatus());
    }

    @PostMapping("/notice/desensitive")
    public ResponseEntity desensitive(@RequestBody RestResponseBody<Map<String, Object>> restResponseBody) {
        synchronized (TaskServiceImpl.getTaskResultMap()) {
            taskService.desensitive(restResponseBody);
        }
        return ResponseEntity.ok("");
    }

    @PostMapping("/notice/extract")
    public ResponseEntity extract(@RequestBody RestResponseBody<ExtractRespVo> restResponseBody) {
        synchronized (TaskServiceImpl.getTaskResultMap()) {
            taskService.extract(restResponseBody);
        }
        return ResponseEntity.ok("");
    }

    @PostMapping("/notice/load")
    public ResponseEntity load(@RequestBody RestResponseBody<LoadRespVo> restResponseBody) {
        synchronized (TaskServiceImpl.getTaskResultMap()) {
            taskService.load(restResponseBody);
        }
        return ResponseEntity.ok("");
    }


    @GetMapping("/statistic")
    public ResponseEntity statisticTasks(@Valid TaskStatisticDto dto) {

        RespBody body = taskService.statisticTasks(dto);

        return new ResponseEntity<>(body, body.getStatus());
    }
}
