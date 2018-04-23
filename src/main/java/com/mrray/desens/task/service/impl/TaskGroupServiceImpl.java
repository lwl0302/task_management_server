package com.mrray.desens.task.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mrray.desens.task.entity.domain.TaskGroup;
import com.mrray.desens.task.entity.dto.TaskGroupAddDto;
import com.mrray.desens.task.entity.dto.TaskGroupPageDto;
import com.mrray.desens.task.entity.dto.TaskGroupUpdateDto;
import com.mrray.desens.task.entity.vo.PageQueryVo;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.entity.vo.TaskGroupPageVo;
import com.mrray.desens.task.repository.TaskGroupRepository;
import com.mrray.desens.task.service.TaskGroupService;
import com.mrray.desens.task.utils.SystemUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TaskGroupServiceImpl implements TaskGroupService {


    private final TaskGroupRepository taskGroupRepository;

    @Autowired
    public TaskGroupServiceImpl(TaskGroupRepository taskGroupRepository) {
        this.taskGroupRepository = taskGroupRepository;
    }


    /**
     * 分页查询任务分组信息
     *
     * @param dto
     * @return
     */
    @Override
    public RespBody findAll(TaskGroupPageDto dto) {

        Pageable pageable = new PageRequest(dto.getPage() - 1, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        Page<TaskGroup> page = taskGroupRepository.findAll((root, query, cb) -> {
            Predicate predicate = root.isNotNull();
            return predicate;
        }, pageable);

        PageQueryVo<TaskGroupPageVo> vo = new PageQueryVo<>();
        SystemUtils.pageResultMapper(page, dto, vo);

        vo.setContent(
                page.getContent().stream().map(TaskGroupPageVo::convert).collect(Collectors.toList())
        );

        return new RespBody<>().setData(vo);
    }

    @Override
    public RespBody findAll(String groupName) {

        Sort sort = new Sort(Sort.Direction.DESC, "createAt");

        List<TaskGroup> all = taskGroupRepository.findAll((root, query, cb) -> {
            Predicate predicate = root.isNotNull();

            if (StringUtils.hasText(groupName)) {
                predicate = cb.and(predicate, cb.like(root.get("groupName").as(String.class), "%" + groupName + "%"));
            }

            return predicate;
        }, sort);

        return new RespBody<>().setData(
                all.stream().map(TaskGroupPageVo::convert).collect(Collectors.toList())
        );
    }

    @Override
    public RespBody save(TaskGroupAddDto dto) {

        String groupName = dto.getGroupName();

        TaskGroup group = taskGroupRepository.findByGroupName(groupName);

        if (group != null) {
            return new RespBody<>().setMessage("groupName has exist");
        }

        group = dto.convert2TaskGroup();

        taskGroupRepository.saveAndFlush(group);

        JSONObject object = new JSONObject();
        object.put("uuid", group.getUuid());

        return new RespBody<>().setData(object).setStatus(HttpStatus.CREATED);
    }

    @Override
    public RespBody save(TaskGroupUpdateDto dto) {

        String uuid = dto.getUuid();

        TaskGroup group = taskGroupRepository.findByUuid(uuid);

        if (group == null) {
            return new RespBody<>().setStatus(HttpStatus.BAD_REQUEST).setMessage("group not exist.");
        }

        BeanUtils.copyProperties(dto, group);

        taskGroupRepository.save(group);

        return new RespBody<>();
    }
}
