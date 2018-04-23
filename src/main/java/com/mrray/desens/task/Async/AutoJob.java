package com.mrray.desens.task.Async;

import com.mrray.desens.task.constant.Constant;
import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.entity.vo.ProgressMessage;
import com.mrray.desens.task.repository.EmailRepository;
import com.mrray.desens.task.repository.TaskLoggingRepository;
import com.mrray.desens.task.repository.TaskRepository;
import com.mrray.desens.task.utils.MessageUtil;
import com.mrray.desens.task.utils.SystemUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

public class AutoJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutoJob");


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
        AsyncTask asyncTask = (AsyncTask) mergedJobDataMap.get("asyncTask");
        TaskLoggingRepository taskLoggingRepository = (TaskLoggingRepository) mergedJobDataMap.get("taskLoggingRepository");
        TaskRepository taskRepository = (TaskRepository) mergedJobDataMap.get("taskRepository");
        String taskId = (String) mergedJobDataMap.get("taskId");
        SimpMessagingTemplate simpMessagingTemplate = (SimpMessagingTemplate) mergedJobDataMap.get("simpMessagingTemplate");
        Task task = taskRepository.findByUuid(taskId);

        EmailRepository emailRepository = (EmailRepository) mergedJobDataMap.get("emailRepository");
        int limit = (int) mergedJobDataMap.get("limit");

        ProgressMessage progressMessage = new ProgressMessage();
        progressMessage.setTaskId(task.getUuid());
        progressMessage.setAuto(task.isAuto());

        String control;
        int taskStatus = task.getStatus();
        if (task.isDeleted()) {
            //control = "任务已删除,自动执行不成功";
            LOGGER.info("task id : " + task.getId() + " 任务已删除,自动执行不成功");

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskJumpMessage(task.getId()));
        } else if (taskStatus == Constant.TASK_STATUS_RUNNING) {
            //control = "任务正在进行,自动执行不成功";

            LOGGER.info("task id : " + task.getId() + " 任务正在进行,自动执行不成功");

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskJumpMessage(task.getId()));
        } else if (taskStatus == Constant.TASK_STATUS_CANCLE) {
            //control = "任务已取消,自动执行不成功";

            LOGGER.info("task id : " + task.getId() + " 任务已取消,自动执行不成功");

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskJumpMessage(task.getId()));
        } else if (taskStatus == Constant.TASK_STATUS_PAUSE) {
            //control = "任务已暂停,自动执行不成功";

            LOGGER.info("task id : " + task.getId() + " 任务已暂停,自动执行不成功");

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskJumpMessage(task.getId()));
        } else if (taskStatus == Constant.TASK_STATUS_FINISH && !task.isPlus()) {
            //control = "普通任务已完成,自动执行不成功";

            LOGGER.info("task id : " + task.getId() + " 普通任务已完成,自动执行不成功");

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskJumpMessage(task.getId()));
        } else if (taskStatus == Constant.TASK_STATUS_WAITING) {

            LOGGER.info("task id : " + task.getId() + " 任务等待中,自动执行不成功");

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskJumpMessage(task.getId()));
        } else {

            if (taskRepository.countByStatusAndDeleted(Constant.TASK_STATUS_RUNNING, Constant.TASK_NOT_DELETED) >= limit) {

                task.setStatus(Constant.TASK_STATUS_WAITING);
                task.setStart(new Date());
                taskRepository.save(task);

                control = "等待执行";

                LOGGER.info("task id : " + task.getId() + " 等待执行");

                progressMessage.setProgress(control);

            } else {
                LOGGER.info("task id : " + task.getId() + " 自动执行");
                control = "自动执行";

                task.setProcessData(0L);
                task.setStatus(Constant.TASK_STATUS_RUNNING);
                taskRepository.save(task);
                progressMessage.setProgress("进行中");

                asyncTask.runTask(task, null, null);
            }

            simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

            SystemUtils.saveTaskLog(task, taskLoggingRepository, control);


        }

    }
}
