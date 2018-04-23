package com.mrray.desens.task.utils;

import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.entity.domain.TaskLogging;
import com.mrray.desens.task.entity.dto.PageQueryDto;
import com.mrray.desens.task.entity.vo.PageQueryVo;
import com.mrray.desens.task.repository.TaskLoggingRepository;
import org.springframework.data.domain.Page;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Arthur on 2017/7/21.
 */
public final class SystemUtils {

    public static void pageResultMapper(Page page, PageQueryDto dto, PageQueryVo pageVo) {
        int currentPage = dto.getPage();
        pageVo.setPage(currentPage);
        pageVo.setCurrentPageElements(page.getNumberOfElements());
        pageVo.setSize(dto.getSize());
        int totalPage = page.getTotalPages();
        pageVo.setTotalPage(totalPage);
        pageVo.setTotalElements(page.getTotalElements());
        pageVo.setPrevPage(currentPage > 1 ? currentPage - 1 : 1);
        pageVo.setFirstPage(currentPage == 1);
        pageVo.setNextPage(currentPage < totalPage ? currentPage + 1 : totalPage > 0 ? totalPage : 1);
        pageVo.setLastPage(currentPage == totalPage || totalPage == 0);
        pageVo.setProperty(dto.getProperty());
        pageVo.setDirection(dto.getDirection());
    }

    public static Map<String, List<String>> getWeek() {
        Date now = new Date();
        SimpleDateFormat showFormat = new SimpleDateFormat("MM-dd");
        SimpleDateFormat conditionFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = conditionFormat.format(now);
        long timestamp = now.getTime() - ((Integer.parseInt(time.substring(11, 13)) * 60 + Integer.parseInt(time.substring(14, 16))) * 60 + Integer.parseInt(time.substring(17, 19))) * 1000;
        Map<String, List<String>> result = new HashMap<>();
        List<String> condition = new ArrayList<>();
        List<String> show = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            long l = timestamp - 24 * 60 * 60 * 1000 * i;
            condition.add(conditionFormat.format(l));
            show.add(showFormat.format(l));
        }
        result.put("condition", condition);
        result.put("show", show);
        return result;
    }

    public static Map<String, List<Object>> getDate() {
        Date now = new Date();
        SimpleDateFormat showFormat = new SimpleDateFormat("MM-dd HH:mm");
        SimpleDateFormat conditionFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = conditionFormat.format(now);
        Integer hour = Integer.parseInt(time.substring(11, 13));
        long timestamp = now.getTime() - (((hour - (hour / 2) * 2) * 60 + Integer.parseInt(time.substring(14, 16))) * 60 + Integer.parseInt(time.substring(17, 19))) * 1000;
        Map<String, List<Object>> result = new HashMap<>();
        List<Object> condition = new ArrayList<>();
        List<Object> show = new ArrayList<>();
        List<Object> stamp = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            long l = timestamp - 2 * 60 * 60 * 1000 * i;
            stamp.add(l);
            condition.add(conditionFormat.format(l));
            show.add(showFormat.format(l));
        }
        result.put("condition", condition);
        result.put("show", show);
        result.put("stamp", stamp);
        return result;
    }

    public static void setType(Task task, TaskLogging taskLogging) {
        boolean plus = task.isPlus();
        boolean auto = task.isAuto();
        if (plus) {
            if (auto) {
                taskLogging.setType("增量任务(自动)");
            } else {
                taskLogging.setType("增量任务(手动)");
            }
        } else {
            taskLogging.setType("普通任务");
        }
    }

    public static void saveTaskLog(Task task, TaskLoggingRepository taskLoggingRepository, String control) {
        TaskLogging taskLogging = new TaskLogging();
        taskLogging.setTask(task.getId());
        taskLogging.setSourceIp(task.getSourceIp());
        taskLogging.setSourceDatabase(task.getSourceDatabase());
        taskLogging.setSourcePort(task.getSourcePort());
        taskLogging.setTargetIp(task.getTargetIp());
        taskLogging.setTargetDatabase(task.getTargetDatabase());
        taskLogging.setTargetPort(task.getTargetPort());
        taskLogging.setSourceType(task.getSourceType());
        taskLogging.setTargetType(task.getTargetType());
        taskLogging.setProcessData(task.getProcessData());
        taskLogging.setSourceName(task.getSourceName());
        taskLogging.setTargetName(task.getTargetName());

        setType(task, taskLogging);
        taskLogging.setControl(control);
        taskLoggingRepository.save(taskLogging);
    }

    public static String parse(Long cost) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(cost);
    }
}