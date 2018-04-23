package com.mrray.desens.task.repository;

import com.mrray.desens.task.entity.domain.IncrementTaskLogging;
import com.mrray.desens.task.entity.domain.Task;

public interface IncrementTaskLoggingRepository extends BaseRepository<IncrementTaskLogging> {


    /**
     * 按照时间倒序查询第一条数据
     *
     * @param task 任务
     * @return
     */
    IncrementTaskLogging findFirstByTaskOrderByCreateAtDesc(Task task);

}
