package com.mrray.desens.task.Async;

import com.mrray.desens.task.constant.Constant;
import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.repository.EmailRepository;
import com.mrray.desens.task.repository.TaskLoggingRepository;
import com.mrray.desens.task.repository.TaskRepository;
import com.mrray.desens.task.service.IncrementTaskLoggingService;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class ResumeJob {
    private final TaskRepository taskRepository;
    private final AsyncTask asyncTask;
    private final TaskLoggingRepository taskLoggingRepository;
    private final EmailRepository emailRepository;

    private final IncrementTaskLoggingService incrementTaskLoggingService;
    private final Scheduler scheduler;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Value("${task.limit}")
    private int limit;//并行任务数限制

    @Autowired
    public ResumeJob(TaskRepository taskRepository, AsyncTask asyncTask, TaskLoggingRepository taskLoggingRepository, Scheduler scheduler, EmailRepository emailRepository, IncrementTaskLoggingService incrementTaskLoggingService, SimpMessagingTemplate simpMessagingTemplate) {
        this.taskRepository = taskRepository;
        this.asyncTask = asyncTask;
        this.taskLoggingRepository = taskLoggingRepository;
        this.scheduler = scheduler;
        this.emailRepository = emailRepository;
        this.incrementTaskLoggingService = incrementTaskLoggingService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @PostConstruct
    public void resume() {
        List<Task> running = taskRepository.findAllByStatus(Constant.TASK_STATUS_RUNNING);
        for (Task task : running) {
            task.setStatus(Constant.TASK_STATUS_FIAL_UNKNOWN);
            task.setError("服务器异常停机,导致失败!");
        }
        taskRepository.save(running);

        asyncTask.startWaitingTask(null);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Task> tasks = taskRepository.findByAuto(true);
        for (Task task : tasks) {
            String uuid = task.getUuid();
            System.out.println("start task : " + uuid);
            JobKey jobKey = new JobKey(uuid, "group1");
            TriggerKey triggerKey = new TriggerKey(uuid, "group1");
            JobDetailImpl jobDetail = new JobDetailImpl();
            jobDetail.setKey(jobKey);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("taskId", task.getUuid());
            jobDataMap.put("asyncTask", asyncTask);
            jobDataMap.put("taskLoggingRepository", taskLoggingRepository);
            jobDataMap.put("taskRepository", taskRepository);
            jobDataMap.put("emailRepository", emailRepository);
            jobDataMap.put("simpMessagingTemplate", simpMessagingTemplate);
            jobDataMap.put("limit", limit);
            jobDetail.setJobDataMap(jobDataMap);
            jobDetail.setJobClass(AutoJob.class);
            try {
                if (!scheduler.isStarted() || scheduler.isShutdown()) {
                    scheduler.start();
                }
                if (scheduler.checkExists(jobKey)) {
                    scheduler.deleteJob(jobKey);
                    //taskRepository.saveAndFlush(task);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if ("INTERVAL".equalsIgnoreCase(task.getCycle())) {
                SimpleTriggerImpl trigger = new SimpleTriggerImpl();
                trigger.setKey(triggerKey);
                trigger.setRepeatInterval(Long.parseLong(task.getCron()));
                trigger.setRepeatCount(-1);
                Date startDate = task.getAutoStartTime();
                if (startDate == null) {
                    trigger.setStartTime(new Date());
                } else {
                    trigger.setStartTime(startDate);
                }
                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            } else {
                if ("IN_TIME".equalsIgnoreCase(task.getCycle())) {
                    String intTimeString = task.getYear() + "-" + task.getMonth() + "-" + task.getPoint() + " " + task.getExecTime();
                    try {
                        Date inTime = format.parse(intTimeString);
                        if (inTime.before(new Date())) {
                            continue;
                        }
                    } catch (ParseException e) {
                        continue;
                    }
                }
                CronTriggerImpl trigger = new CronTriggerImpl();
                trigger.setKey(triggerKey);
                try {
                    trigger.setCronExpression(task.getCron());
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }
}
