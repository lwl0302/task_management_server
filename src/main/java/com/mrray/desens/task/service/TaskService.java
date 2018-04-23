package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.dto.AutoDto;
import com.mrray.desens.task.entity.dto.TaskAddDto;
import com.mrray.desens.task.entity.dto.TaskQueryDto;
import com.mrray.desens.task.entity.dto.TaskStatisticDto;
import com.mrray.desens.task.entity.vo.ExtractRespVo;
import com.mrray.desens.task.entity.vo.LoadRespVo;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.entity.vo.RestResponseBody;

import java.util.Map;

/**
 * Created by Arthur on 2017/7/21.
 */
public interface TaskService {

    /**
     * 根据给定的条件查询任务列表
     *
     * @param dto 查询条件封装
     * @return 返回分页知乎的任务列表
     */
    RespBody getTasks(TaskQueryDto dto);

    /**
     * 新建和在任务未启动前更新任务
     *
     * @param uuid
     * @param dto
     * @return
     */
    RespBody addAndupdateTask(String uuid, TaskAddDto dto);

    RespBody startTask(String uuid);

    RespBody pauseTask(String uuid);

    RespBody cancelTask(String uuid);

    void desensitive(RestResponseBody<Map<String, Object>> restResponseBody);

    void extract(RestResponseBody<ExtractRespVo> restResponseBody);

    void load(RestResponseBody<LoadRespVo> restResponseBody);

    RespBody index();

    RespBody updateDB(String uuid);

    RespBody plus(String uuid, String colunmName, String columnType);

    RespBody auto(String uuid, AutoDto autoDto);

    RespBody delete(String[] uuid);

    RespBody statisticTasks(TaskStatisticDto dto);

}
