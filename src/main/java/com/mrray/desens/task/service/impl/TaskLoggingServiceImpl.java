package com.mrray.desens.task.service.impl;

import com.mrray.desens.task.constant.Constant;
import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.entity.domain.TaskLogging;
import com.mrray.desens.task.entity.dto.TaskLoggingPageDto;
import com.mrray.desens.task.entity.vo.PageQueryVo;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.entity.vo.TaskLoggingPageVo;
import com.mrray.desens.task.repository.TaskLoggingRepository;
import com.mrray.desens.task.repository.TaskRepository;
import com.mrray.desens.task.service.TaskLoggingService;
import com.mrray.desens.task.utils.SystemUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskLoggingServiceImpl implements TaskLoggingService {

    private final TaskLoggingRepository taskLoggingRepository;

    private final TaskRepository taskRepository;

    @Autowired
    public TaskLoggingServiceImpl(TaskLoggingRepository taskLoggingRepository, TaskRepository taskRepository) {
        this.taskLoggingRepository = taskLoggingRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public RespBody page(TaskLoggingPageDto dto) {

        List<Task> list = taskRepository.findAll((root, query, cb) -> {

            Predicate predicate = root.isNotNull();

            Date beginTime = dto.getBeginTime();
            if (beginTime != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createAt").as(Date.class), beginTime));
            }

            Date endTime = dto.getEndTime();
            if (endTime != null) {
                predicate = cb.and(predicate, cb.lessThan(root.get("createAt").as(Date.class), endTime));
            }

            String taskType = dto.getTaskType();
            // taskType 不传表示所有的任务
            if (StringUtils.isNotBlank(taskType)) {
                // 取消的任务
                if ("Cancel".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_CANCLE));
                    // Failed 表示失败的任务
                } else if ("Failed".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.lessThan(root.get("status").as(int.class), Constant.TASK_STATUS_NEW));
                    // Finished 表示失败的任务
                } else if ("Finished".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_FINISH));
                    // Pause 表示失败的任务
                } else if ("Pause".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_PAUSE));
                    // NotStart 表示失败的任务
                } else if ("NotStart".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_NEW));

                    // Removed 表示删除的任务
                } else if ("Removed".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.isTrue(root.get("deleted")));
                }
            }

            return predicate;

        });

        List<Long> uuids = list.stream().map(Task::getId).collect(Collectors.toList());

        PageQueryVo<TaskLoggingPageVo> pageVo = new PageQueryVo<>();
        if (uuids != null && uuids.size() > 0) {
            Pageable pageable = new PageRequest(dto.getPage() - 1, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

            Page<TaskLogging> page = taskLoggingRepository.findAll((root, query, cb) -> {
                Predicate predicate = root.isNotNull();

                Path<Object> task = root.get("task");

                predicate = cb.and(predicate, task.in(uuids));

                Date beginTime = dto.getBeginTime();
                if (beginTime != null) {
                    predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createAt").as(Date.class), beginTime));
                }

                Date endTime = dto.getEndTime();
                if (endTime != null) {
                    predicate = cb.and(predicate, cb.lessThan(root.get("createAt").as(Date.class), endTime));
                }

                return predicate;
            }, pageable);

            SystemUtils.pageResultMapper(page, dto, pageVo);

            pageVo.setContent(
                    page.getContent().stream().map(TaskLoggingPageVo::convert).collect(Collectors.toList())
            );
        } else {
            BeanUtils.copyProperties(dto, pageVo);
        }

        return new RespBody<>().setData(pageVo);
    }
}
