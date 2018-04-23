package com.mrray.desens.task.repository;

import com.mrray.desens.task.entity.domain.TaskLogging;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by Arthur on 2017/7/20.
 */
public interface TaskLoggingRepository extends BaseRepository<TaskLogging> {

    List<TaskLogging> findByTask(Long uuid);

    Page<TaskLogging> findByTaskIn(List<String> uuids, Pageable pageable);

}
