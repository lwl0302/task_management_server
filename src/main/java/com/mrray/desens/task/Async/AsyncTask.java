package com.mrray.desens.task.Async;

import com.mrray.desens.task.constant.Constant;
import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.entity.domain.TaskStatus;
import com.mrray.desens.task.entity.dto.*;
import com.mrray.desens.task.entity.vo.ExtractRespVo;
import com.mrray.desens.task.entity.vo.LoadRespVo;
import com.mrray.desens.task.entity.vo.ProgressMessage;
import com.mrray.desens.task.entity.vo.RestResponseBody;
import com.mrray.desens.task.repository.EmailRepository;
import com.mrray.desens.task.repository.TaskLoggingRepository;
import com.mrray.desens.task.repository.TaskRepository;
import com.mrray.desens.task.service.DataDesensitiveService;
import com.mrray.desens.task.service.DatasourceManagementService;
import com.mrray.desens.task.service.IncrementTaskLoggingService;
import com.mrray.desens.task.service.impl.TaskServiceImpl;
import com.mrray.desens.task.utils.DatabaseUtil;
import com.mrray.desens.task.utils.MessageUtil;
import com.mrray.desens.task.utils.SystemUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AsyncTask {
    @Value("${task.rows}")
    private Long rows;//单次处理行数

    @Value("${task.limit}")
    private int limit;//并行任务数限制

    @Value("${task.extractLength}")
    private int extractLength;//抽取队列数

    private final TaskRepository taskRepository;


    private final DatasourceManagementService datasourceManagementService;

    private final DataDesensitiveService dataDesensitiveService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final EmailRepository emailRepository;

    private final IncrementTaskLoggingService loggingService;

    private final Scheduler scheduler;

    private final TaskLoggingRepository taskLoggingRepository;

    private Log logger = LogFactory.getLog(AsyncTask.class);

    @Autowired
    public AsyncTask(TaskRepository taskRepository, DatasourceManagementService datasourceManagementService, DataDesensitiveService dataDesensitiveService, SimpMessagingTemplate simpMessagingTemplate, EmailRepository emailRepository, IncrementTaskLoggingService loggingService, Scheduler scheduler, TaskLoggingRepository taskLoggingRepository) {
        this.taskRepository = taskRepository;
        this.dataDesensitiveService = dataDesensitiveService;
        this.datasourceManagementService = datasourceManagementService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.emailRepository = emailRepository;
        this.loggingService = loggingService;
        this.scheduler = scheduler;
        this.taskLoggingRepository = taskLoggingRepository;
    }


    @Async
    public void runTask(Task task, String beforeRunTye, Boolean auto) {


        synchronized (TaskServiceImpl.getTaskResultMap()) {
            /*Long oldDone = task.getDone();

            task = taskRepository.findByUuid(task.getUuid());

            if (!oldDone.equals(task.getDone())) {
                StringBuilder doneLogBuilder = new StringBuilder("beforeRunTye: ").append(beforeRunTye).append(" oldDone: ").append(oldDone).append(", newDone: ").append(task.getDone());
                logger.info(doneLogBuilder);
            }*/

            int status = task.getStatus();

            if (status == Constant.TASK_STATUS_CANCLE || status == Constant.TASK_STATUS_FINISH || status < Constant.TASK_STATUS_NEW) {
                //删除终止状态的中间表
                Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().remove(task.getUuid());
                if (taskResult != null) {
                    List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                    List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                    ArrayList<String> tables = new ArrayList<String>();
                    for (ExtractRespVo extract : extractList) {
                        tables.addAll(extract.getTableNames());
                    }
                    for (Map<String, Object> desens : desensList) {

                        LinkedHashMap sourceInfo = (LinkedHashMap) desens.get("databaseInfo");
                        tables.add((String) sourceInfo.get("tableName"));
                    }
                    if (!CollectionUtils.isEmpty(tables)) {
                        DatabaseUtil.dropTablesAsyn(extractList.get(0), tables);
                    }

                }
                return;
            } else if (status == Constant.TASK_STATUS_PAUSE) {
                if (Constant.TASK_TYPE_EXTRCT.equals(beforeRunTye) || Constant.TASK_TYPE_DESENS.equals(beforeRunTye)) {
                    return;
                } else if (Constant.TASK_TYPE_LOAD.equals(beforeRunTye)) {
                    Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().remove(task.getUuid());
                    if (taskResult != null) {
                        List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                        List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                        ArrayList<String> tables = new ArrayList<String>();
                        for (ExtractRespVo extract : extractList) {
                            tables.addAll(extract.getTableNames());
                        }
                        for (Map<String, Object> desens : desensList) {

                            LinkedHashMap sourceInfo = (LinkedHashMap) desens.get("databaseInfo");
                            tables.add((String) sourceInfo.get("tableName"));
                        }
                        if (!CollectionUtils.isEmpty(tables)) {
                            DatabaseUtil.dropTablesAsyn(extractList.get(0), tables);
                        }
                    }

                    // 发送邮件
                    ProgressMessage progressMessage = new ProgressMessage();
                    progressMessage.setProgress("已暂停");
                    progressMessage.setTaskId(task.getUuid());
                    progressMessage.setAuto(task.isAuto());
                    simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
                } else {
                    return;
                }

            }


            if (StringUtils.isEmpty(beforeRunTye)) {//初始化任务,启动第一次抽取

                if (task.isPlus()) {
                    // 记录日志
                    loggingService.save(task, auto, false, null, TaskStatus.EXTRACT);
                }

                Map<String, List> taskResult = new HashMap<>();
                taskResult.put(Constant.TASK_TYPE_EXTRCT, new ArrayList<ExtractRespVo>());
                taskResult.put(Constant.TASK_TYPE_DESENS, new ArrayList<Map<String, Object>>());
                taskResult.put(Constant.TASK_TYPE_LOAD, new ArrayList<LoadRespVo>());
                TaskServiceImpl.getTaskResultMap().put(task.getUuid(), taskResult);

                ExtractDto extractDto = new ExtractDto();
                extractDto.setMainTaskId(task.getUuid());
                extractDto.setTableName(task.getSourceFile());
                extractDto.setId(task.getSource());
                extractDto.setRows(rows);
                boolean plus = task.isPlus();
                extractDto.setPlus(plus);
                if (plus) {
                    extractDto.setDatePlus(task.getDatePlus());
                    extractDto.setIntPlus(task.getIntPlus());
                    extractDto.setColumnName(task.getColumnName());
                    extractDto.setColumnType(task.getColumnType());
                    if (extractDto.getIntPlus() != null) {
                        logger.info("任务 " + task.getUuid() + " 启动 执行抽取任务,抽取intPlus: " + extractDto.getIntPlus());
                    } else {
                        logger.info("任务 " + task.getUuid() + " 启动 执行抽取任务,抽取datePlus: " + extractDto.getDatePlus());
                    }
                } else {
                    extractDto.setDone(task.getDone());
                    logger.info("任务 " + task.getUuid() + " 启动 执行抽取任务,抽取index: " + extractDto.getDone());
                }
                task.setStart(new Date());

                extractDto.setFields(task.getFields());
                execuseExtract(task, extractDto);

            } /*else if (Constant.TASK_TYPE_EXTRCT.equals(beforeRunTye)) {//抽取后的操作
                Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().get(task.getUuid());
                if (taskResult != null) {
                    List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                    List<LoadRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);
                    ExtractRespVo extractResult = extractList.get(extractList.size() - 1);
                    if (rows <= extractResult.getCount() && (extractList.size() - loadList.size()) <= extractLength && !extractResult.isStop()) {
                        ExtractDto extractDto = new ExtractDto();
                        extractDto.setMainTaskId(task.getUuid());
                        extractDto.setTableName(task.getSourceFile());
                        extractDto.setId(task.getSource());
                        extractDto.setRows(rows);
                        boolean plus = task.isPlus();
                        extractDto.setPlus(plus);
                        if (plus) {
                            extractDto.setDatePlus(extractResult.getDatePlus());
                            extractDto.setIntPlus(extractResult.getIntPlus());
                            extractDto.setColumnName(task.getColumnName());
                            extractDto.setColumnType(task.getColumnType());
                            if (extractDto.getIntPlus() != null) {
                                logger.info("任务 " + task.getUuid() + " 抽取回调 执行抽取任务,抽取intPlus: " + extractDto.getIntPlus());
                            } else {
                                logger.info("任务 " + task.getUuid() + " 抽取回调 执行抽取任务,抽取datePlus: " + extractDto.getDatePlus());
                            }
                        } else {
                            Long done = task.getDone();
                            for (int i = loadList.size(); i < extractList.size(); i++) {
                                done += extractList.get(i).getCount();
                            }
                            extractDto.setDone(done);
                            logger.info("任务 " + task.getUuid() + " 抽取回调 执行抽取任务,抽取index: " + extractDto.getDone());
                        }

                        extractDto.setFields(task.getFields());
                        execuseExtract(task, extractDto);
                    }
                    List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                    if (desensList.size() == extractList.size() - 1) {
                        DatabaseInfo databaseInfo = new DatabaseInfo();
                        BeanUtils.copyProperties(extractResult, databaseInfo);
                        databaseInfo.setTableName(extractResult.getTableNames().get(0));
                        execuseDesens(task, databaseInfo);
                    }
                }

            } else if (Constant.TASK_TYPE_DESENS.equals(beforeRunTye)) {
                Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().get(task.getUuid());
                if (taskResult != null) {
                    List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                    List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                    List<LoadRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);
                    Map<String, Object> desensResult = desensList.get(desensList.size() - 1);
                    if (extractList.size() > desensList.size()) {
                        ExtractRespVo extractResult = extractList.get(desensList.size());
                        DatabaseInfo databaseInfo = new DatabaseInfo();
                        BeanUtils.copyProperties(extractResult, databaseInfo);
                        databaseInfo.setTableName(extractResult.getTableNames().get(0));
                        execuseDesens(task, databaseInfo);
                    }
                    if (loadList.size() == desensList.size() - 1) {
                        DatabaseInfo sourceInfo = new DatabaseInfo();
                        LinkedHashMap databaseInfo = (LinkedHashMap) desensResult.get("databaseInfo");
                        sourceInfo.setIp((String) databaseInfo.get("ip"));
                        sourceInfo.setDatabaseName((String) databaseInfo.get("databaseName"));
                        sourceInfo.setPort((Integer) databaseInfo.get("port"));
                        sourceInfo.setMainTaskId((String) databaseInfo.get("mainTaskId"));
                        sourceInfo.setUsername((String) databaseInfo.get("username"));
                        sourceInfo.setPassword((String) databaseInfo.get("password"));
                        sourceInfo.setTableName((String) databaseInfo.get("tableName"));

                        //DatabaseInfo sourceInfo = (DatabaseInfo) desensResult.get("databaseInfo");

                        LoadDto loadDto = new LoadDto();
                        loadDto.setExtractId(task.getExtract());
                        loadDto.setMainTaskId(task.getUuid());

                        SourceInfoDto sourceInfoDto = new SourceInfoDto();
                        BeanUtils.copyProperties(sourceInfo, sourceInfoDto);
                        sourceInfoDto.setDbType("mysql");

                        TargetInfoDto targetInfoDto = new TargetInfoDto();
                        targetInfoDto.setFileType(task.getTargetType());
                        targetInfoDto.setId(task.getTarget());
                        targetInfoDto.setTableName(task.getTargetFile());

                        loadDto.setSourceInfo(sourceInfoDto);
                        loadDto.setTargetInfo(targetInfoDto);

                        execuseLoad(task, loadDto);
                    }
                }


            } else if (Constant.TASK_TYPE_LOAD.equals(beforeRunTye)) {
                Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().get(task.getUuid());
                if (taskResult != null) {
                    List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                    List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                    List<ExtractRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);

                    if (desensList.size() > loadList.size()) {
                        Map<String, Object> desensResult = desensList.get(loadList.size());

                        DatabaseInfo sourceInfo = new DatabaseInfo();
                        LinkedHashMap databaseInfo = (LinkedHashMap) desensResult.get("databaseInfo");
                        sourceInfo.setIp((String) databaseInfo.get("ip"));
                        sourceInfo.setDatabaseName((String) databaseInfo.get("databaseName"));
                        sourceInfo.setPort((Integer) databaseInfo.get("port"));
                        sourceInfo.setMainTaskId((String) databaseInfo.get("mainTaskId"));
                        sourceInfo.setUsername((String) databaseInfo.get("username"));
                        sourceInfo.setPassword((String) databaseInfo.get("password"));
                        sourceInfo.setTableName((String) databaseInfo.get("tableName"));


                        LoadDto loadDto = new LoadDto();
                        loadDto.setExtractId(task.getExtract());
                        loadDto.setMainTaskId(task.getUuid());

                        SourceInfoDto sourceInfoDto = new SourceInfoDto();
                        BeanUtils.copyProperties(sourceInfo, sourceInfoDto);
                        sourceInfoDto.setDbType("mysql");

                        TargetInfoDto targetInfoDto = new TargetInfoDto();
                        targetInfoDto.setFileType(task.getTargetType());
                        targetInfoDto.setId(task.getTarget());
                        targetInfoDto.setTableName(task.getTargetFile());

                        loadDto.setSourceInfo(sourceInfoDto);
                        loadDto.setTargetInfo(targetInfoDto);

                        execuseLoad(task, loadDto);

                    }

                    ExtractRespVo extractResult = extractList.get(extractList.size() - 1);
                    if (rows <= extractResult.getCount() && (extractList.size() - loadList.size()) == extractLength && !extractResult.isStop()) {
                        ExtractDto extractDto = new ExtractDto();
                        extractDto.setMainTaskId(task.getUuid());
                        extractDto.setTableName(task.getSourceFile());
                        extractDto.setId(task.getSource());
                        extractDto.setRows(rows);
                        boolean plus = task.isPlus();
                        extractDto.setPlus(plus);
                        if (plus) {
                            extractDto.setDatePlus(extractResult.getDatePlus());
                            extractDto.setIntPlus(extractResult.getIntPlus());
                            extractDto.setColumnName(task.getColumnName());
                            extractDto.setColumnType(task.getColumnType());

                            if (extractDto.getIntPlus() != null) {
                                logger.info("任务 " + task.getUuid() + " 装载回调 执行抽取任务,抽取intPlus: " + extractDto.getIntPlus());
                            } else {
                                logger.info("任务 " + task.getUuid() + " 装载回调 执行抽取任务,抽取datePlus: " + extractDto.getDatePlus());
                            }
                        } else {
                            Long done = task.getDone();
                            for (int i = loadList.size(); i < extractList.size(); i++) {
                                done += extractList.get(i).getCount();
                            }
                            extractDto.setDone(done);

                            logger.info("任务 " + task.getUuid() + " 装载回调 执行抽取任务,抽取index: " + extractDto.getDone());
                        }
                        extractDto.setFields(task.getFields());
                        execuseExtract(task, extractDto);
                    }

                    int removeIndex = loadList.size() - 1;
                    loadList.remove(removeIndex);
                    desensList.remove(removeIndex);
                    extractList.remove(removeIndex);
                }
            }*/

        }
    }


    public void taskCallback(Task task, String beforeRunTye, Boolean auto) {

        Long oldDone = task.getDone();

        Long newDone = taskRepository.findByUuid(task.getUuid()).getDone();

        if (!oldDone.equals(newDone)) {
            StringBuilder doneLogBuilder = new StringBuilder("beforeRunTye: ").append(beforeRunTye).append(" oldDone: ").append(oldDone).append(", newDone: ").append(newDone);
            logger.info(doneLogBuilder);
        }

        int status = task.getStatus();

        if (status == Constant.TASK_STATUS_CANCLE || status == Constant.TASK_STATUS_FINISH || status < Constant.TASK_STATUS_NEW) {
            //删除终止状态的中间表
            Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().remove(task.getUuid());
            if (taskResult != null) {
                List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                ArrayList<String> tables = new ArrayList<String>();
                for (ExtractRespVo extract : extractList) {
                    tables.addAll(extract.getTableNames());
                }
                for (Map<String, Object> desens : desensList) {

                    LinkedHashMap sourceInfo = (LinkedHashMap) desens.get("databaseInfo");
                    tables.add((String) sourceInfo.get("tableName"));
                }
                if (!CollectionUtils.isEmpty(tables)) {
                    DatabaseUtil.dropTablesAsyn(extractList.get(0), tables);
                }

            }
            return;
        } else if (status == Constant.TASK_STATUS_PAUSE) {
            if (Constant.TASK_TYPE_EXTRCT.equals(beforeRunTye) || Constant.TASK_TYPE_DESENS.equals(beforeRunTye)) {
                return;
            } else if (Constant.TASK_TYPE_LOAD.equals(beforeRunTye)) {
                Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().remove(task.getUuid());
                if (taskResult != null) {
                    List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                    List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                    ArrayList<String> tables = new ArrayList<String>();
                    for (ExtractRespVo extract : extractList) {
                        tables.addAll(extract.getTableNames());
                    }
                    for (Map<String, Object> desens : desensList) {

                        LinkedHashMap sourceInfo = (LinkedHashMap) desens.get("databaseInfo");
                        tables.add((String) sourceInfo.get("tableName"));
                    }
                    if (!CollectionUtils.isEmpty(tables)) {
                        DatabaseUtil.dropTablesAsyn(extractList.get(0), tables);
                    }
                }

                // 发送邮件
                ProgressMessage progressMessage = new ProgressMessage();
                progressMessage.setProgress("已暂停");
                progressMessage.setTaskId(task.getUuid());
                progressMessage.setAuto(task.isAuto());
                simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
            } else {
                return;
            }

        }

        if (Constant.TASK_TYPE_EXTRCT.equals(beforeRunTye)) {//抽取后的操作
            Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().get(task.getUuid());
            if (taskResult != null) {
                List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                List<LoadRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);
                ExtractRespVo extractResult = extractList.get(extractList.size() - 1);
                if (rows <= extractResult.getCount() && (extractList.size() - loadList.size()) <= extractLength && !extractResult.isStop()) {
                    ExtractDto extractDto = new ExtractDto();
                    extractDto.setMainTaskId(task.getUuid());
                    extractDto.setTableName(task.getSourceFile());
                    extractDto.setId(task.getSource());
                    extractDto.setRows(rows);
                    boolean plus = task.isPlus();
                    extractDto.setPlus(plus);
                    if (plus) {
                        extractDto.setDatePlus(extractResult.getDatePlus());
                        extractDto.setIntPlus(extractResult.getIntPlus());
                        extractDto.setColumnName(task.getColumnName());
                        extractDto.setColumnType(task.getColumnType());
                        if (extractDto.getIntPlus() != null) {
                            logger.info("任务 " + task.getUuid() + " 抽取回调 执行抽取任务,抽取intPlus: " + extractDto.getIntPlus());
                        } else {
                            logger.info("任务 " + task.getUuid() + " 抽取回调 执行抽取任务,抽取datePlus: " + extractDto.getDatePlus());
                        }
                    } else {
                        Long done = task.getDone();
                        for (int i = loadList.size(); i < extractList.size(); i++) {
                            done += extractList.get(i).getCount();
                        }
                        extractDto.setDone(done);
                        logger.info("任务 " + task.getUuid() + " 抽取回调 执行抽取任务,抽取index: " + extractDto.getDone());
                    }

                    extractDto.setFields(task.getFields());
                    execuseExtract(task, extractDto);
                }
                List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                if (desensList.size() == extractList.size() - 1) {
                    DatabaseInfo databaseInfo = new DatabaseInfo();
                    BeanUtils.copyProperties(extractResult, databaseInfo);
                    databaseInfo.setTableName(extractResult.getTableNames().get(0));
                    execuseDesens(task, databaseInfo);
                }
            }

        } else if (Constant.TASK_TYPE_DESENS.equals(beforeRunTye)) {
            Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().get(task.getUuid());
            if (taskResult != null) {
                List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                List<LoadRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);
                Map<String, Object> desensResult = desensList.get(desensList.size() - 1);
                if (extractList.size() > desensList.size()) {
                    ExtractRespVo extractResult = extractList.get(desensList.size());
                    DatabaseInfo databaseInfo = new DatabaseInfo();
                    BeanUtils.copyProperties(extractResult, databaseInfo);
                    databaseInfo.setTableName(extractResult.getTableNames().get(0));
                    execuseDesens(task, databaseInfo);
                }
                if (loadList.size() == desensList.size() - 1) {
                    DatabaseInfo sourceInfo = new DatabaseInfo();
                    LinkedHashMap databaseInfo = (LinkedHashMap) desensResult.get("databaseInfo");
                    sourceInfo.setIp((String) databaseInfo.get("ip"));
                    sourceInfo.setDatabaseName((String) databaseInfo.get("databaseName"));
                    sourceInfo.setPort((Integer) databaseInfo.get("port"));
                    sourceInfo.setMainTaskId((String) databaseInfo.get("mainTaskId"));
                    sourceInfo.setUsername((String) databaseInfo.get("username"));
                    sourceInfo.setPassword((String) databaseInfo.get("password"));
                    sourceInfo.setTableName((String) databaseInfo.get("tableName"));

                    //DatabaseInfo sourceInfo = (DatabaseInfo) desensResult.get("databaseInfo");

                    LoadDto loadDto = new LoadDto();
                    loadDto.setExtractId(task.getExtract());
                    loadDto.setMainTaskId(task.getUuid());

                    SourceInfoDto sourceInfoDto = new SourceInfoDto();
                    BeanUtils.copyProperties(sourceInfo, sourceInfoDto);
                    sourceInfoDto.setDbType("mysql");

                    TargetInfoDto targetInfoDto = new TargetInfoDto();
                    targetInfoDto.setFileType(task.getTargetType());
                    targetInfoDto.setId(task.getTarget());
                    targetInfoDto.setTableName(task.getTargetFile());

                    loadDto.setSourceInfo(sourceInfoDto);
                    loadDto.setTargetInfo(targetInfoDto);

                    execuseLoad(task, loadDto);
                }
            }


        } else if (Constant.TASK_TYPE_LOAD.equals(beforeRunTye)) {
            Map<String, List> taskResult = TaskServiceImpl.getTaskResultMap().get(task.getUuid());
            if (taskResult != null) {
                List<ExtractRespVo> extractList = taskResult.get(Constant.TASK_TYPE_EXTRCT);
                List<Map<String, Object>> desensList = taskResult.get(Constant.TASK_TYPE_DESENS);
                List<ExtractRespVo> loadList = taskResult.get(Constant.TASK_TYPE_LOAD);

                if (desensList.size() > loadList.size()) {
                    Map<String, Object> desensResult = desensList.get(loadList.size());

                    DatabaseInfo sourceInfo = new DatabaseInfo();
                    LinkedHashMap databaseInfo = (LinkedHashMap) desensResult.get("databaseInfo");
                    sourceInfo.setIp((String) databaseInfo.get("ip"));
                    sourceInfo.setDatabaseName((String) databaseInfo.get("databaseName"));
                    sourceInfo.setPort((Integer) databaseInfo.get("port"));
                    sourceInfo.setMainTaskId((String) databaseInfo.get("mainTaskId"));
                    sourceInfo.setUsername((String) databaseInfo.get("username"));
                    sourceInfo.setPassword((String) databaseInfo.get("password"));
                    sourceInfo.setTableName((String) databaseInfo.get("tableName"));


                    LoadDto loadDto = new LoadDto();
                    loadDto.setExtractId(task.getExtract());
                    loadDto.setMainTaskId(task.getUuid());

                    SourceInfoDto sourceInfoDto = new SourceInfoDto();
                    BeanUtils.copyProperties(sourceInfo, sourceInfoDto);
                    sourceInfoDto.setDbType("mysql");

                    TargetInfoDto targetInfoDto = new TargetInfoDto();
                    targetInfoDto.setFileType(task.getTargetType());
                    targetInfoDto.setId(task.getTarget());
                    targetInfoDto.setTableName(task.getTargetFile());

                    loadDto.setSourceInfo(sourceInfoDto);
                    loadDto.setTargetInfo(targetInfoDto);

                    execuseLoad(task, loadDto);

                }

                ExtractRespVo extractResult = extractList.get(extractList.size() - 1);
                if (rows <= extractResult.getCount() && (extractList.size() - loadList.size()) == extractLength && !extractResult.isStop()) {
                    ExtractDto extractDto = new ExtractDto();
                    extractDto.setMainTaskId(task.getUuid());
                    extractDto.setTableName(task.getSourceFile());
                    extractDto.setId(task.getSource());
                    extractDto.setRows(rows);
                    boolean plus = task.isPlus();
                    extractDto.setPlus(plus);
                    if (plus) {
                        extractDto.setDatePlus(extractResult.getDatePlus());
                        extractDto.setIntPlus(extractResult.getIntPlus());
                        extractDto.setColumnName(task.getColumnName());
                        extractDto.setColumnType(task.getColumnType());

                        if (extractDto.getIntPlus() != null) {
                            logger.info("任务 " + task.getUuid() + " 装载回调 执行抽取任务,抽取intPlus: " + extractDto.getIntPlus());
                        } else {
                            logger.info("任务 " + task.getUuid() + " 装载回调 执行抽取任务,抽取datePlus: " + extractDto.getDatePlus());
                        }
                    } else {
                        Long done = task.getDone();
                        for (int i = loadList.size(); i < extractList.size(); i++) {
                            done += extractList.get(i).getCount();
                        }
                        extractDto.setDone(done);

                        logger.info("任务 " + task.getUuid() + " 装载回调 执行抽取任务,抽取index: " + extractDto.getDone());
                    }
                    extractDto.setFields(task.getFields());
                    execuseExtract(task, extractDto);
                }

                int removeIndex = loadList.size() - 1;
                loadList.remove(removeIndex);
                desensList.remove(removeIndex);
                extractList.remove(removeIndex);
            }
        }

    }

    private void execuseLoad(Task task, LoadDto loadDto) {
        RestResponseBody load;
        try {
            load = datasourceManagementService.load(loadDto);
            task.setLoads(((Map<String, String>) load.getData()).get("taskId"));
        } catch (Exception e) {
            task.setStatus(Constant.TASK_STATUS_FIAL_LOAD);
            task.setError("连接装载服务失败");

            ProgressMessage progressMessage = new ProgressMessage();
            progressMessage.setTaskId(task.getUuid());
            progressMessage.setProgress("装载失败");
            progressMessage.setAuto(task.isAuto());


            if (task.isPlus()) {
                loggingService.update(task, true, null, TaskStatus.LOAD);
            }

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskFailedMessage(task.getId(), "装载", task.getError()));

            progressMessage.setError(task.getError());
            simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

            startWaitingTask(task.getId());

        }
        taskRepository.save(task);
    }

    private void execuseDesens(Task task, DatabaseInfo databaseInfo) {
        RestResponseBody desensitive;
        ProgressMessage progressMessage = new ProgressMessage();
        progressMessage.setTaskId(task.getUuid());
        progressMessage.setAuto(task.isAuto());
        try {
            desensitive = dataDesensitiveService.desensitive(databaseInfo, task.getPolicy(), task.getScan());
        } catch (Exception e) {
            task.setStatus(Constant.TASK_STATUS_FIAL_DESENS);
            task.setError("连接脱敏服务失败");
            taskRepository.saveAndFlush(task);

            progressMessage.setProgress("脱敏失败");

            if (task.isPlus()) {
                loggingService.update(task, true, null, TaskStatus.DESENS);
            }

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskFailedMessage(task.getId(), "脱敏", task.getError()));

            progressMessage.setError(task.getError());
            simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

            startWaitingTask(task.getId());

            return;
        }
        if (Constant.MICRO_SERVER_RESULT_SUCCESS.equalsIgnoreCase(desensitive.getMessage())) {
            task.setDesensitive(((Map<String, String>) desensitive.getData()).get("taskUuid"));
        } else {
            task.setError(desensitive.getError());
            task.setStatus(Constant.TASK_STATUS_FIAL_DESENS);

            progressMessage.setProgress("脱敏失败");

            if (task.isPlus()) {
                loggingService.update(task, true, null, TaskStatus.DESENS);
            }

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskFailedMessage(task.getId(), "脱敏", task.getError()));

            progressMessage.setError(desensitive.getError());
            simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
            startWaitingTask(task.getId());
        }
        taskRepository.save(task);
    }

    private void execuseExtract(Task task, ExtractDto extractDto) {
        RestResponseBody extract;
        try {
            extract = datasourceManagementService.extract(extractDto);
            task.setExtract(((Map<String, String>) extract.getData()).get("taskId"));
        } catch (Exception e) {
            task.setError("连接抽取服务失败");
            task.setStatus(Constant.TASK_STATUS_FIAL_EXTRACT);

            // 发送邮件
            MessageUtil.sendEmail(emailRepository, MessageUtil.getTaskFailedMessage(task.getId(), "抽取", task.getError()));

            if (task.isPlus()) {
                loggingService.update(task, true, null, TaskStatus.EXTRACT);
            }

            ProgressMessage progressMessage = new ProgressMessage();
            progressMessage.setTaskId(task.getUuid());
            progressMessage.setAuto(task.isAuto());
            progressMessage.setProgress("抽取失败");
            progressMessage.setError(task.getError());
            simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

            startWaitingTask(task.getId());
        }
        taskRepository.save(task);
    }

    private String parse(Long cost) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(cost);
    }


    /**
     * 任务完成开始等待中的任务
     *
     * @param preRunningTaskId 前一个终止任务的id
     */
    public synchronized void startWaitingTask(Long preRunningTaskId) {

        //仅执行一次的自动任务,执行后设置自动为false
        if (preRunningTaskId != null) {
            Task task = taskRepository.findOne(preRunningTaskId);
            if (task != null && task.isAuto()) {

                if ("IN_TIME".equalsIgnoreCase(task.getCycle()) && new Date().getTime() > execTime(task)) {
                    task.setAuto(false);
                    taskRepository.save(task);
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
                    ProgressMessage progressMessage = new ProgressMessage();
                    progressMessage.setTaskId(task.getUuid());
                    progressMessage.setAuto(task.isAuto());
                    simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);
                }
            }

        }

        Long runningTaskCount = taskRepository.countByStatusAndDeleted(Constant.TASK_STATUS_RUNNING, Constant.TASK_NOT_DELETED);
        if (runningTaskCount >= limit) {
            return;
        } else {
            long startCount = limit - runningTaskCount;
            List<Task> taskList = taskRepository.findByStatusOrderByStartLimit(Constant.TASK_STATUS_WAITING, startCount);
            ProgressMessage progressMessage = new ProgressMessage();
            for (Task task : taskList) {
                task.setProcessData(0l);
                task.setStatus(Constant.TASK_STATUS_RUNNING);
                taskRepository.save(task);

                progressMessage.setTaskId(task.getUuid());
                progressMessage.setProgress("进行中");
                progressMessage.setAuto(task.isAuto());
                simpMessagingTemplate.convertAndSend("/topic/progress", progressMessage);

                String control = task.isAuto() ? "自动执行" : "启动任务";
                SystemUtils.saveTaskLog(task, taskLoggingRepository, control);
                runTask(task, null, null);
            }
        }

    }

    private long execTime(Task task) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = task.getYear() + "-" + task.getMonth() + "-" + task.getPoint() + " " + task.getExecTime();
        try {
            return simpleDateFormat.parse(s).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }
}
