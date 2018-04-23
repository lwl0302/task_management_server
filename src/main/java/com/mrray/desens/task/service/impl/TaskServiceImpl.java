package com.mrray.desens.task.service.impl;

import com.mrray.desens.task.Async.AsyncTask;
import com.mrray.desens.task.Async.AutoJob;
import com.mrray.desens.task.constant.Constant;
import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.entity.domain.TaskGroup;
import com.mrray.desens.task.entity.domain.TaskLogging;
import com.mrray.desens.task.entity.domain.TaskStatus;
import com.mrray.desens.task.entity.dto.*;
import com.mrray.desens.task.entity.vo.*;
import com.mrray.desens.task.repository.EmailRepository;
import com.mrray.desens.task.repository.TaskGroupRepository;
import com.mrray.desens.task.repository.TaskLoggingRepository;
import com.mrray.desens.task.repository.TaskRepository;
import com.mrray.desens.task.service.IncrementTaskLoggingService;
import com.mrray.desens.task.service.TaskService;
import com.mrray.desens.task.utils.DatabaseUtil;
import com.mrray.desens.task.utils.LicenseUtils;
import com.mrray.desens.task.utils.MessageUtil;
import com.mrray.desens.task.utils.SystemUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Arthur on 2017/7/21.
 */

@Service
public class TaskServiceImpl implements TaskService {

    private static final Map<String, Map<String, List>> taskResultMap = new Hashtable<String, Map<String, List>>();

    @Value("${task.limit}")
    private int limit;//并行任务数限制

    @Value("${task.rows}")
    private int rows;//单次处理行数

    private static final List<String> FILE_TYPE = Arrays.asList("csv", "txt");

    private final TaskRepository taskRepository;


  /*  private final DatasourceManagementService datasourceManagementService;

    private final DataDesensitiveService dataDesensitiveService;*/

    private final AsyncTask asyncTask;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final Scheduler scheduler;

    private final TaskGroupRepository taskGroupRepository;

    private final TaskLoggingRepository taskLoggingRepository;

    private final EmailRepository emailRepository;

    private final IncrementTaskLoggingService loggingService;

    private Log logger = LogFactory.getLog(TaskServiceImpl.class);

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository,
                           AsyncTask asyncTask,
                           SimpMessagingTemplate simpMessagingTemplate,
                           Scheduler scheduler,
                           TaskGroupRepository taskGroupRepository, TaskLoggingRepository taskLoggingRepository, EmailRepository emailRepository, IncrementTaskLoggingService loggingService) {
        this.taskRepository = taskRepository;
        this.asyncTask = asyncTask;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.scheduler = scheduler;
        this.taskGroupRepository = taskGroupRepository;
        this.taskLoggingRepository = taskLoggingRepository;
        this.emailRepository = emailRepository;
        this.loggingService = loggingService;
    }

    @Override
    public RespBody getTasks(TaskQueryDto dto) {

        RespBody<PageQueryVo<TaskQueryVo>> body = new RespBody<>();

        PageQueryVo<TaskQueryVo> pageQueryVo = new PageQueryVo<>();

        Pageable page = new PageRequest(dto.getPage() - 1, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        Page<Task> taskPage = taskRepository.findAll((Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            Predicate predicate = root.isNotNull();

            predicate = cb.and(predicate, cb.isFalse(root.get("deleted")));

            Long uuid = dto.getUuid();
            if (uuid != null) {
                predicate = cb.and(predicate, cb.equal(root.get("id").as(Long.class), uuid));
            }

            Date beginTime = dto.getBeginTime();
            if (beginTime != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createAt").as(Date.class), beginTime));
            }

            Date endTime = dto.getEndTime();
            if (endTime != null) {
                predicate = cb.and(predicate, cb.lessThan(root.get("createAt").as(Date.class), endTime));
            }

            String searchKey = dto.getSearchKey();
            if (StringUtils.isNotEmpty(searchKey)) {
                Predicate uuidLike;
                Predicate searchKeyLike = cb.like(root.get("sourceFile").as(String.class), "%" + searchKey + "%");
                try {
                    Long value = Long.valueOf(searchKey);
                    uuidLike = cb.equal(root.get("id").as(Long.class), value);
                } catch (NumberFormatException e) {
                    uuidLike = cb.equal(root.get("id").as(Long.class), 1);
                }
                predicate = cb.and(predicate, cb.or(uuidLike, searchKeyLike));
            }

            String group = dto.getGroup();
            if (StringUtils.isNotEmpty(group)) {

                // 00000000 表示未分组
                if ("00000000".equalsIgnoreCase(group)) {
                    predicate = cb.and(predicate, cb.isNull(root.get("group")));
                } else {
                    predicate = cb.and(predicate, cb.equal(root.get("group").get("uuid").as(String.class), group));
                }
            }

            String taskType = dto.getTaskType();
            if (StringUtils.isNotBlank(taskType)) {
                if ("Normal".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.isFalse(root.get("plus")));
                } else if ("HandIncrement".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.isTrue(root.get("plus")), cb.isFalse(root.get("auto")));
                } else if ("AutoIncrement".equalsIgnoreCase(taskType)) {
                    predicate = cb.and(predicate, cb.isTrue(root.get("plus")), cb.isTrue(root.get("auto")));
                }
            }

            Integer status = dto.getStatus();
            if (status != null) {
                switch (status) {
                    case -2:
                        predicate = cb.and(predicate, cb.lessThan(root.get("status").as(int.class), Constant.TASK_STATUS_NEW));
                        break;
                    case -1:
                        predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_CANCLE));
                        break;
                    case 0:
                        predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_NEW));
                        break;
                    case 1:
                        predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_RUNNING));
                        break;
                    case 2:
                        predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_PAUSE));
                        break;
                    case 3:
                        predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_FINISH));
                        //predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), 4));
                        break;
                    case 4:
                        predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), Constant.TASK_STATUS_WAITING));
                        //predicate = cb.and(predicate, cb.equal(root.get("status").as(int.class), 5));
                        break;
                }
            }

            Integer policyType = dto.getPolicyType();
            if (policyType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("policyType").as(int.class), policyType));
            }

            String sourceType = dto.getSourceType();
            if (StringUtils.isNotEmpty(sourceType)) {
                predicate = cb.and(predicate, cb.equal(root.get("sourceType").as(String.class), sourceType));
            }

            String targetType = dto.getTargetType();
            if (StringUtils.isNotEmpty(targetType) && targetType.equalsIgnoreCase("file")) {
                CriteriaBuilder.In<String> in = cb.in(root.get("targetType"));
                //TODO 支持新文件类型时此处也需要添加
                in.value("csv");
                predicate = cb.and(predicate, in);
            } else if (StringUtils.isNotEmpty(targetType)) {
                predicate = cb.and(predicate, cb.like(root.get("targetType").as(String.class), "%" + targetType + "%"));
            }

            return predicate;
        }, page);
        SystemUtils.pageResultMapper(taskPage, dto, pageQueryVo);

        pageQueryVo.setContent(taskPage.getContent().stream().map(task -> {
            TaskQueryVo vo = new TaskQueryVo();
            BeanUtils.copyProperties(task, vo);
            Long cost = task.getCost();
            if (cost != null) {
                vo.setTime(parse(cost * 1000));
            }
            if (FILE_TYPE.contains(task.getTargetType())) {
                vo.setTargetType("file");
            }
            vo.setPolicy(task.getPolicyType() == 0 ? String.format("已有规则:%s", task.getPolicyName()) : "手动配置");
            int statusCode = task.getStatus();
            String status = "";
            if (statusCode == Constant.TASK_STATUS_NEW) {
                status = "未启动";
            }
            if (statusCode == Constant.TASK_STATUS_RUNNING) {
                status = "进行中";
            }
            if (statusCode == Constant.TASK_STATUS_PAUSE) {
                status = "已暂停";
            }
            if (statusCode == Constant.TASK_STATUS_CANCLE) {
                status = "已取消";
            }
            if (statusCode == Constant.TASK_STATUS_FINISH) {
                status = "已完成";
            }
            if (statusCode == Constant.TASK_STATUS_WAITING) {
                status = "等待执行";
            }

            if (statusCode == Constant.TASK_STATUS_FIAL_LOAD) {
                status = "装载失败";
            }
            if (statusCode == Constant.TASK_STATUS_FIAL_DESENS) {
                status = "脱敏失败";
            }
            if (statusCode == Constant.TASK_STATUS_FIAL_EXTRACT) {
                status = "抽取失败";
            }
            if (statusCode == Constant.TASK_STATUS_FIAL_UNKNOWN) {
                status = "未知错误";
            }


            TaskGroup group = task.getGroup();
            if (group != null) {
                vo.setGroup(group.getGroupName());
            }


            /*swi
            tch (statusCode) {
                case -4:
                    if (running) {
                        status = "装载中";
                    } else {
                        status = "装载失败";
                    }
                    break;
                case -3:
                    if (running) {
                        status = "脱敏中";
                    } else {
                        status = "脱敏失败";
                    }
                    break;
                case -2:
                    if (running) {
                        status = "扫描中";
                    } else {
                        status = "扫描失败";
                    }
                    break;
                case -1:
                    if (running) {
                        status = "抽取中";
                    } else {
                        status = "抽取失败";
                    }
                    break;
                case 0:
                    if (running) {
                        status = "抽取中";
                    } else {
                        status = "未启动";
                    }
                    break;
                case 1:
                    if (running) {
                        status = "扫描中";
                    }
                    break;
                case 2:
                    if (running) {
                        status = "脱敏中";
                    }
                    break;
                case 3:
                    if (running) {
                        status = "装载中";
                    }
                    break;
                case 4:
                    status = "已完成";
                    break;
                case 5:
                    status = "已完成";
                    break;
                default:
                    status = "";
            }*/
            vo.setStatus(status);
            return vo;
        }).collect(Collectors.toList()));

        body.setData(pageQueryVo);
        return body;
    }

    @Override
    public RespBody addAndupdateTask(String uuid, TaskAddDto dto) {
        String control;
        RespBody<TaskIdentifier> body = new RespBody<>();
        if (!LicenseUtils.isActive()) {
            return body.setMessage("not active");
        }
        Task task;
        if (StringUtils.isEmpty(uuid)) {
            task = new Task();
            control = "新建任务";
        } else {
            task = taskRepository.findByUuid(uuid);
            if (task == null) {
                return body.setStatus(HttpStatus.BAD_REQUEST).setMessage("task not find");
            }
            control = "更新任务";
        }
        // 任务组
        String groupUuid = dto.getGroup();
        if (StringUtils.isNotBlank(groupUuid)) {
            TaskGroup group = taskGroupRepository.findByUuid(groupUuid);
            if (group == null) {
                return new RespBody<>().setMessage("group not exist");
            }
            task.setGroup(group);
        }
        BeanUtils.copyProperties(dto, task);
        if (StringUtils.isNotBlank(dto.getColumnName()) && StringUtils.isNotBlank(dto.getColumnType())) {
            task.setPlus(true);
        }
        try {
            body.setData(save(task));
        } catch (Exception e) {
        }
        SystemUtils.saveTaskLog(taskRepository.findByUuid(task.getUuid()), taskLoggingRepository, control);
        body.setStatus(HttpStatus.CREATED);
        return body;
    }


    @Override
    public RespBody startTask(String uuid) {
        RespBody respBody = new RespBody();
        String control;
        ProgressMessage progressMessage = new ProgressMessage();
        progressMessage.setTaskId(uuid);

        Task task = taskRepository.findByUuid(uuid);
        if (task == null || task.isDeleted()) {
            respBody.setMessage("没有找到此任务,无法执行!");
            respBody.setStatus(HttpStatus.CONFLICT);
            return respBody;
        }
        progressMessage.setAuto(task.isAuto());
        int taskSatus = task.getStatus();
        if (taskSatus == Constant.TASK_STATUS_CANCLE) {
            respBody.setMessage("任务已取消,不可执行!");
            respBody.setStatus(HttpStatus.CONFLICT);
            return respBody;
        }

        if (taskSatus == Constant.TASK_STATUS_FINISH && !task.isPlus()) {
            respBody.setMessage("普通任务已完成,不可再次执行!");
            respBody.setStatus(HttpStatus.CONFLICT);
            return respBody;
        }

        if (taskSatus == Constant.TASK_STATUS_RUNNING) {
            respBody.setMessage("此任务已在执行,不可重复执行!");
            respBody.setStatus(HttpStatus.CONFLICT);
            return respBody;
        }

        if (taskSatus == Constant.TASK_STATUS_PAUSE) {
            synchronized (TaskServiceImpl.taskResultMap) {
                Map<String, List> taskResult = TaskServiceImpl.taskResultMap.get(uuid);
                if (taskResult != null) {
                    List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                    List<LoadRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);
                    if (desensList.size() > loadList.size()) {
                        respBody.setMessage("任务正在暂停中,请稍候重试!");
                        respBody.setStatus(HttpStatus.CONFLICT);
                        return respBody;
                    }
                }
            }
            control = "启动任务";
        } else {
            if (task.getStatus() < 0) {
                control = "重试任务";
            } else {
                control = "开始任务";
            }
        }

        if (taskRepository.countByStatusAndDeleted(Constant.TASK_STATUS_RUNNING, Constant.TASK_NOT_DELETED) >= limit) {

            task.setStatus(Constant.TASK_STATUS_WAITING);
            task.setStart(new Date());
            taskRepository.save(task);

            control = "等待执行";

            progressMessage.setProgress("等待执行");

        } else {

            task.setStatus(Constant.TASK_STATUS_RUNNING);
            task.setProcessData(0L);
            taskRepository.saveAndFlush(task);

            progressMessage.setProgress("进行中");


            asyncTask.runTask(task, null, false);

        }

        simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

        SystemUtils.saveTaskLog(task, taskLoggingRepository, control);

        return respBody;
    }

    @Override
    public void extract(RestResponseBody<ExtractRespVo> restResponseBody) {

        ExtractRespVo data = restResponseBody.getData();
        String tableName = data.getTableNames().size() > 0 ? data.getTableNames().get(0) : null;
        logger.info(data.getMainTaskId() + " 抽取回调:" + tableName + " " + data.getCount());
        Task task = taskRepository.findByUuid(data.getMainTaskId());
        if (Constant.MICRO_SERVER_RESULT_SUCCESS.equalsIgnoreCase(restResponseBody.getMessage())) {
            boolean deleteFlag = false;//是否需要删除中间表
            Map<String, List> taskResult = TaskServiceImpl.taskResultMap.get(data.getMainTaskId());
            if (taskResult != null) {
                List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                extractList.add(data);
            } else {
                deleteFlag = true;
            }
            if (deleteFlag) {
                //删除此次抽取任务的中间表
                DatabaseUtil.dropTablesAsyn(data, data.getTableNames());
            }

            if (task.isPlus()) {
                loggingService.update(task, false, null, TaskStatus.EXTRACT);
            }
        } else {
            if (data != null && !CollectionUtils.isEmpty(data.getTableNames())) {
                DatabaseUtil.dropTablesAsyn(data, data.getTableNames());
            }

            if (Constant.TASK_STATUS_RUNNING == task.getStatus()) {
                task.setError(restResponseBody.getError());
                task.setStatus(Constant.TASK_STATUS_FIAL_EXTRACT);
                taskRepository.save(task);

                ProgressMessage progressMessage = new ProgressMessage();
                progressMessage.setProgress("抽取失败");

                if (task.isPlus()) {
                    loggingService.update(task, true, "抽取失败", TaskStatus.FAILED);
                }

                // 发送邮件
                MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskFailedMessage(task.getId(), "脱敏", restResponseBody.getError()));

                progressMessage.setTaskId(task.getUuid());
                progressMessage.setError(restResponseBody.getError());
                progressMessage.setAuto(task.isAuto());
                simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

                asyncTask.startWaitingTask(task.getId());
            }

        }
        asyncTask.taskCallback(task, Constant.TASK_TYPE_EXTRCT, null);
    }


    @Override
    public RespBody index() {
        RespBody<Object> respBody = new RespBody<>();
        Map<String, Object> result = new HashMap<>();
        respBody.setData(result);
        Long total = taskRepository.count();
        result.put("total", total);
        Long running = taskRepository.countByStatusAndDeleted(Constant.TASK_STATUS_RUNNING, Constant.TASK_NOT_DELETED);
        result.put("running", running);
        Long cost = taskRepository.avgCost();
        result.put("cost", parse(cost * 1000));
        Map<String, List<String>> week = SystemUtils.getWeek();
        List<List<Object>> tasks = new ArrayList<>();
        SimpleDateFormat conditionFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < 7; i++) {
            List<Object> task = new ArrayList<>();
            task.add(week.get("show").get(i));
            List<String> condition = week.get("condition");
            try {
                if (i == 0) {
                    task.add(taskRepository.dayTasks(conditionFormat.parse(condition.get(i)), new Date()));
                } else {
                    task.add(taskRepository.dayTasks(conditionFormat.parse(condition.get(i)), conditionFormat.parse(condition.get(i - 1))));
                }
            } catch (ParseException e) {
            }
            tasks.add(task);
        }
        result.put("tasks", tasks);
        return respBody;
    }

    @Override
    public RespBody updateDB(String uuid) {
        return null;
    }

    @Override
    public RespBody plus(String uuid, String colunmName, String columnType) {
        return null;
    }

    @Override
    public RespBody auto(String uuid, AutoDto autoDto) {
        RespBody respBody = new RespBody();
        Task task = taskRepository.findByUuid(uuid);
        boolean auto = autoDto.isAuto();
        JobKey jobKey = new JobKey(uuid, "group1");
        if (auto) {

            if (!task.isPlus()) {
                if (!"IN_TIME".equalsIgnoreCase(autoDto.getCycle())) {
                    respBody.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                    respBody.setMessage("非增量任务只能设置仅执行一次的任务!");
                    return respBody;
                } else if (Constant.TASK_STATUS_RUNNING == task.getStatus()) {
                    respBody.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                    respBody.setMessage("非增量任务进行中,不能设置执行时间!");
                    return respBody;
                } else if (Constant.TASK_STATUS_PAUSE == task.getStatus()) {
                    respBody.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                    respBody.setMessage("非增量任务暂停中,不能设置执行时间!");
                    return respBody;

                } else if (Constant.TASK_STATUS_CANCLE == task.getStatus()) {
                    respBody.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                    respBody.setMessage("非增量任务已取消,不能设置执行时间!");
                    return respBody;

                } else if (Constant.TASK_STATUS_FINISH == task.getStatus()) {
                    respBody.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                    respBody.setMessage("非增量任务已完成,不能设置执行时间!");
                    return respBody;

                } else if (Constant.TASK_STATUS_FIAL_UNKNOWN == task.getStatus()) {
                    respBody.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                    respBody.setMessage("非增量任务未知错误,不能设置执行时间!");
                    return respBody;

                } else if (Constant.TASK_STATUS_WAITING == task.getStatus()) {
                    respBody.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                    respBody.setMessage("非增量任务等待中,不能设置执行时间!");
                    return respBody;

                }
            }

            task.setAuto(true);
            String cron = autoDto.getCronExp();
            task.setCron(cron);
            task.setPoint(autoDto.getPoint());
            task.setCycle(autoDto.getCycle());
            task.setExecTime(autoDto.getExecTime());
            task.setMonth(autoDto.getMonth());
            task.setYear(autoDto.getYear());
            taskRepository.saveAndFlush(task);
            //start
            TriggerKey triggerKey = new TriggerKey(uuid, "group1");
            JobDetailImpl jobDetail = new JobDetailImpl();
            jobDetail.setKey(jobKey);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("taskId", task.getUuid());
            jobDataMap.put("asyncTask", asyncTask);
            jobDataMap.put("taskRepository", taskRepository);
            jobDataMap.put("taskLoggingRepository", taskLoggingRepository);
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
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }


            if (!"INTERVAL".equalsIgnoreCase(autoDto.getCycle())) {
                CronTriggerImpl trigger = new CronTriggerImpl();
                trigger.setKey(triggerKey);
                try {
                    trigger.setCronExpression(cron);
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            } else {
                SimpleTriggerImpl trigger = new SimpleTriggerImpl();
                trigger.setKey(triggerKey);
                trigger.setRepeatInterval(Long.parseLong(cron));
                trigger.setRepeatCount(-1);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String startTime = autoDto.getStartTime();
                Date date;
                if (StringUtils.isNotBlank(startTime)) {
                    try {
                        date = sdf.parse(startTime);
                    } catch (ParseException e) {
                        date = new Date();
                    }
                } else {
                    date = new Date();
                }
                task.setAutoStartTime(date);
                trigger.setStartTime(date);
                taskRepository.saveAndFlush(task);

                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }

        } else {
            task.setAuto(false);
            try {
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException e) {
                //e.printStackTrace();
            }
        }
        taskRepository.saveAndFlush(task);
        return respBody;
    }

    @Override
    public RespBody delete(String[] uuids) {

        List<Task> deletedTask = new ArrayList<>(uuids.length);

        Arrays.stream(uuids).forEach(uuid -> {
            Task task = taskRepository.findByUuid(uuid);
            if (task != null && task.getStatus() != Constant.TASK_STATUS_RUNNING) {
                task.delete();
                deletedTask.add(task);

                //取消自动任务
                if (task.isAuto()) {
                    task.setAuto(false);
                    JobKey jobKey = new JobKey(task.getUuid(), "group1");
                    try {
                        if (!scheduler.isStarted() || scheduler.isShutdown()) {
                            scheduler.start();
                        }
                        if (scheduler.checkExists(jobKey)) {
                            scheduler.deleteJob(jobKey);
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }

            }
        });

        taskRepository.save(deletedTask);
        return new RespBody<>();
    }

    @Override
    public RespBody statisticTasks(TaskStatisticDto dto) {

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

            return predicate;

        });

        TaskStatisticVo vo = new TaskStatisticVo();

        vo.setTotalTasks(list.size());
        list.forEach(task -> {

            // 完成的任务
            if (task.getStatus() == Constant.TASK_STATUS_FINISH) {
                vo.setFinishedTasks(vo.getFinishedTasks() + 1);
            } else if (task.isDeleted()) {
                vo.setRemovedTasks(vo.getRemovedTasks() + 1);
            }

            // 暂停的任务
            else if (task.getStatus() == Constant.TASK_STATUS_PAUSE) {
                vo.setPauseTasks(vo.getPauseTasks() + 1);
            }

            // 取消的任务
            else if (task.getStatus() == Constant.TASK_STATUS_CANCLE) {
                vo.setCancelTasks(vo.getCancelTasks() + 1);
            }

            // 失败的
            else if (task.getStatus() < Constant.TASK_STATUS_NEW) {
                vo.setFailedTasks(vo.getFailedTasks() + 1);
            }

            // 未启动状态码为0  没有运行没有暂停没有取消
            else if (task.getStatus() == Constant.TASK_STATUS_NEW) {
                vo.setNotstartTasks(vo.getNotstartTasks() + 1);
            }

            // 正在进行中的任务
            if (task.isPlus()) {
                // 增量任务
                if (task.isAuto()) {
                    // 自动
                    vo.setAutoIncrementTasks(vo.getAutoIncrementTasks() + 1);
                } else {
                    // 手动
                    vo.setHandIncrementTasks(vo.getHandIncrementTasks() + 1);
                }
            } else {
                // 普通任务
                vo.setNormalTasks(vo.getNormalTasks() + 1);
            }

            // 处理的数据条数
            Long done = task.getDone();
            done = done == null ? 0 : done - 1;
            vo.setTotalProcessData(vo.getTotalProcessData() + done);

            if (task.isPlus()) {
                List<TaskLogging> loggings = taskLoggingRepository.findByTask(task.getId());
                final Date beginTime = dto.getBeginTime();
                final Date endTime = dto.getEndTime();
                if (beginTime != null && endTime != null) {
                    loggings.stream()
                            .filter(log -> log.getCreateAt().after(beginTime) && log.getCreateAt().before(endTime) && "任务结束".equals(log.getControl()))
                            .forEach(log -> vo.setIncrementProcessData(vo.getIncrementProcessData() + log.getProcessData()));
                } else if (beginTime != null && endTime == null) {
                    loggings.stream()
                            .filter(log -> log.getCreateAt().after(beginTime) && "任务结束".equals(log.getControl()))
                            .forEach(log -> vo.setIncrementProcessData(vo.getIncrementProcessData() + log.getProcessData()));
                } else if (beginTime == null && endTime != null) {
                    loggings.stream()
                            .filter(log -> log.getCreateAt().before(endTime) && "任务结束".equals(log.getControl()))
                            .forEach(log -> vo.setIncrementProcessData(vo.getIncrementProcessData() + log.getProcessData()));
                } else {
                    loggings.stream()
                            .filter(log -> "任务结束".equals(log.getControl()))
                            .forEach(log -> vo.setIncrementProcessData(vo.getIncrementProcessData() + log.getProcessData()));
                }
            }

        });

        return new RespBody<>().setData(vo);
    }

    private String parse(Long cost) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(cost);
    }

    @Override
    public void desensitive(RestResponseBody<Map<String, Object>> restResponseBody) {
        Map<String, Object> progress = restResponseBody.getData();
        logger.info((String) progress.get("mainTaskId") + " 脱敏回调:" + ((LinkedHashMap) progress.get("databaseInfo")).get("tableName"));
        Task task = taskRepository.findByUuid((String) progress.get("mainTaskId"));
        if (Constant.MICRO_SERVER_RESULT_SUCCESS.equalsIgnoreCase(restResponseBody.getMessage())) {

            boolean deleteFlag = false;//是否需要删除中间表
            Map<String, List> taskResult = TaskServiceImpl.taskResultMap.get(task.getUuid());
            if (taskResult != null) {
                List desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                desensList.add(progress);
            } else {
                deleteFlag = true;
            }
            if (deleteFlag) {
                //删除此次脱敏任务的中间表
                DatabaseInfo sourceInfo = new DatabaseInfo();
                LinkedHashMap databaseInfo = (LinkedHashMap) progress.get("databaseInfo");
                sourceInfo.setIp((String) databaseInfo.get("ip"));
                sourceInfo.setDatabaseName((String) databaseInfo.get("databaseName"));
                sourceInfo.setPort((Integer) databaseInfo.get("port"));
                sourceInfo.setMainTaskId((String) databaseInfo.get("mainTaskId"));
                sourceInfo.setUsername((String) databaseInfo.get("username"));
                sourceInfo.setPassword((String) databaseInfo.get("password"));
                sourceInfo.setTableName((String) databaseInfo.get("tableName"));

                BaseResourceInfoVo baseResourceInfo = new BaseResourceInfoVo();
                BeanUtils.copyProperties(sourceInfo, baseResourceInfo);
                baseResourceInfo.setDbType(Constant.DATABASE_TYPE_MYSQL);
                ArrayList<String> tables = new ArrayList<String>();
                tables.add(sourceInfo.getTableName());
                DatabaseUtil.dropTablesAsyn(baseResourceInfo, tables);
            }

            if (task.isPlus()) {
                loggingService.update(task, false, null, TaskStatus.DESENS);
            }

        } else {
            LinkedHashMap databaseInfo = (LinkedHashMap) progress.get("databaseInfo");
            if (databaseInfo != null && !StringUtils.isEmpty((String) databaseInfo.get("tableName"))) {
                DatabaseInfo sourceInfo = new DatabaseInfo();
                sourceInfo.setIp((String) databaseInfo.get("ip"));
                sourceInfo.setDatabaseName((String) databaseInfo.get("databaseName"));
                sourceInfo.setPort((Integer) databaseInfo.get("port"));
                sourceInfo.setMainTaskId((String) databaseInfo.get("mainTaskId"));
                sourceInfo.setUsername((String) databaseInfo.get("username"));
                sourceInfo.setPassword((String) databaseInfo.get("password"));
                sourceInfo.setTableName((String) databaseInfo.get("tableName"));

                BaseResourceInfoVo baseResourceInfo = new BaseResourceInfoVo();
                BeanUtils.copyProperties(sourceInfo, baseResourceInfo);
                baseResourceInfo.setDbType(Constant.DATABASE_TYPE_MYSQL);
                ArrayList<String> tables = new ArrayList<String>();
                tables.add(sourceInfo.getTableName());
                DatabaseUtil.dropTablesAsyn(baseResourceInfo, tables);
            }

            if (task.getStatus() == Constant.TASK_STATUS_RUNNING) {
                task.setError(restResponseBody.getError());
                task.setStatus(Constant.TASK_STATUS_FIAL_DESENS);
                taskRepository.save(task);

                ProgressMessage progressMessage = new ProgressMessage();
                progressMessage.setProgress("脱敏失败");

                if (task.isPlus()) {
                    loggingService.update(task, true, "脱敏失败", TaskStatus.FAILED);
                }

                // 发送邮件
                MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskFailedMessage(task.getId(), "脱敏", restResponseBody.getError()));

                progressMessage.setTaskId(task.getUuid());
                progressMessage.setError(restResponseBody.getError());
                progressMessage.setAuto(task.isAuto());
                simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
                asyncTask.startWaitingTask(task.getId());
            }
        }
        asyncTask.taskCallback(task, Constant.TASK_TYPE_DESENS, null);

    }


    @Override
    public void load(RestResponseBody<LoadRespVo> restResponseBody) {
        LoadRespVo loadResp = restResponseBody.getData();
        logger.info(loadResp.getMainTaskId() + " 装载回调:" + loadResp.getCount());
        Task task = taskRepository.findByUuid(loadResp.getMainTaskId());
        ProgressMessage progressMessage = new ProgressMessage();
        progressMessage.setTaskId(task.getUuid());
        progressMessage.setAuto(task.isAuto());

        if (Constant.MICRO_SERVER_RESULT_SUCCESS.equalsIgnoreCase(restResponseBody.getMessage())) {
            Map<String, List> taskResult = TaskServiceImpl.taskResultMap.get(task.getUuid());
            if (taskResult != null) {
                List<LoadRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);
                List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                loadList.add(loadResp);
                task.setProcessData(task.getProcessData() + loadResp.getCount());
                task.setDone(task.getDone() + loadResp.getCount());
                ExtractRespVo extractResp = extractList.get(loadList.size() - 1);
                if (task.isPlus()) {
                    if (extractResp.getIntPlus() != null) {
                        task.setIntPlus(extractResp.getIntPlus());
                    }
                    if (extractResp.getDatePlus() != null) {
                        task.setDatePlus(extractResp.getDatePlus());
                    }
                }
                task.setCost((System.currentTimeMillis() - task.getStart().getTime()) / 1000);

                if (Constant.TASK_STATUS_RUNNING == task.getStatus()) {
                    if (loadResp.getCount() < rows || extractResp.isStop()) {
                        task.setStatus(Constant.TASK_STATUS_FINISH);
                        progressMessage.setProgress("已完成");
                        progressMessage.setTime(parse(task.getCost() * 1000));
                        simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
                        SystemUtils.saveTaskLog(task, taskLoggingRepository, "任务结束");
                        taskRepository.save(task);
                        asyncTask.startWaitingTask(task.getId());
                    } else {
                        progressMessage.setProgress("已完成" + (task.getDone() - 1) + "条");
                        progressMessage.setTime(parse(task.getCost() * 1000));
                        simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
                    }
                }

                taskRepository.save(task);
            }


            if (task.isPlus()) {
                loggingService.update(task, false, null, TaskStatus.LOAD);
            }

        } else {

            if (task.getStatus() == Constant.TASK_STATUS_RUNNING) {
                task.setError(restResponseBody.getError());
                task.setStatus(Constant.TASK_STATUS_FIAL_LOAD);
                taskRepository.save(task);

                progressMessage.setProgress("装载失败");


                if (task.isPlus()) {
                    loggingService.update(task, true, "装载失败", TaskStatus.FAILED);
                }

                // 发送邮件
                MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskFailedMessage(task.getId(), "装载", restResponseBody.getError()));

                progressMessage.setError(restResponseBody.getError());
                simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
                asyncTask.startWaitingTask(task.getId());
            }

        }

        asyncTask.taskCallback(task, Constant.TASK_TYPE_LOAD, null);
    }


    @Override
    public RespBody pauseTask(String uuid) {
        RespBody<Task> respBody = new RespBody<>();

        Task task = taskRepository.findByUuid(uuid);
        if (task.getStatus() != Constant.TASK_STATUS_RUNNING && task.getStatus() != Constant.TASK_STATUS_WAITING) {
            respBody.setStatus(HttpStatus.CONFLICT);
            respBody.setMessage("非进行中的任务,不可暂停!");
            return respBody;
        }

        SystemUtils.saveTaskLog(task, taskLoggingRepository, "暂停任务");
        task.setStatus(Constant.TASK_STATUS_PAUSE);
        taskRepository.save(task);

        String progress;
        synchronized (TaskServiceImpl.taskResultMap) {
            Map<String, List> taskResult = TaskServiceImpl.taskResultMap.get(uuid);
            List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
            List<LoadRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);
            if (desensList.size() > loadList.size()) {
                progress = "已暂停";
            } else {
                progress = "已暂停";
                TaskServiceImpl.taskResultMap.remove(uuid);
            }
        }

        ProgressMessage progressMessage = new ProgressMessage();
        progressMessage.setProgress(progress);
        progressMessage.setTaskId(uuid);
        progressMessage.setAuto(task.isAuto());
        simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

        asyncTask.startWaitingTask(task.getId());

        respBody.setData(task);
        return respBody;
    }

    @Override
    public RespBody cancelTask(String uuid) {
        Task task = taskRepository.findByUuid(uuid);
        SystemUtils.saveTaskLog(task, taskLoggingRepository, "取消任务");
        task.setStatus(Constant.TASK_STATUS_CANCLE);

        //取消自动任务
        if (task.isAuto()) {
            JobKey jobKey = new JobKey(task.getUuid(), "group1");
            try {
                if (!scheduler.isStarted() || scheduler.isShutdown()) {
                    scheduler.start();
                }
                if (scheduler.checkExists(jobKey)) {
                    scheduler.deleteJob(jobKey);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            task.setAuto(false);
        }

        taskRepository.save(task);
        ProgressMessage progressMessage = new ProgressMessage();
        progressMessage.setProgress("已取消");
        progressMessage.setTaskId(uuid);
        progressMessage.setAuto(task.isAuto());
        simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

        asyncTask.startWaitingTask(task.getId());

        RespBody<Task> respBody = new RespBody<>();
        respBody.setData(task);
        return respBody;
    }


    private TaskIdentifier save(Task task) {
        try {
            taskRepository.save(task);
        } catch (DataIntegrityViolationException e) {
            task.setUuid(RandomStringUtils.random(8, true, true).toLowerCase());
            save(task);
        }
        return new TaskIdentifier(task.getId());
    }

    public static Map<String, Map<String, List>> getTaskResultMap() {
        return taskResultMap;
    }

}