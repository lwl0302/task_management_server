package com.mrray.desens.task.service.impl;

import com.mrray.desens.task.entity.domain.IncrementTaskLogging;
import com.mrray.desens.task.entity.domain.Task;
import com.mrray.desens.task.entity.domain.TaskStatus;
import com.mrray.desens.task.entity.dto.IncremTaskLoggingPageDto;
import com.mrray.desens.task.entity.vo.IncremTaskLoggingPageVo;
import com.mrray.desens.task.entity.vo.PageQueryVo;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.repository.IncrementTaskLoggingRepository;
import com.mrray.desens.task.service.IncrementTaskLoggingService;
import com.mrray.desens.task.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class IncrementTaskLoggingServiceImpl implements IncrementTaskLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(IncrementTaskLoggingServiceImpl.class);

    private final IncrementTaskLoggingRepository incrementTaskLoggingRepository;


    @Autowired
    public IncrementTaskLoggingServiceImpl(IncrementTaskLoggingRepository incrementTaskLoggingRepository) {
        this.incrementTaskLoggingRepository = incrementTaskLoggingRepository;
    }

    @Override
    public boolean save(Task task, Boolean auto, boolean hasError, String error, TaskStatus status) {

        try {
            IncrementTaskLogging logging = new IncrementTaskLogging();
            logging.setTask(task);
            if (auto != null) {
                logging.setAuto(auto);
            } else {
                logging.setAuto(task.isAuto());
            }
            logging.setProcData(task.getProcessData());

            if (hasError) {
                logging.setSuccess(false);
                if (!StringUtils.hasText(error)) {
                    error = task.getError();
                }
                logging.setError(error);
                logging.setProcData(null);
            }
            logging.setFinishAt(new Date());

            incrementTaskLoggingRepository.save(logging);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            return false;
        }

        return true;
    }

    @Override
    public boolean update(Task task, boolean hasError, String error, TaskStatus status) {

        try {
            IncrementTaskLogging logging = incrementTaskLoggingRepository.findFirstByTaskOrderByCreateAtDesc(task);
            if (logging == null) {
                logging = new IncrementTaskLogging();
                logging.setAuto(task.isAuto());
            }

            logging.setTask(task);
            logging.setProcData(task.getProcessData());

            if (hasError) {
                logging.setSuccess(false);
                if (!StringUtils.hasText(error)) {
                    error = task.getError();
                }
                logging.setError(error);
                logging.setProcData(null);
            }
            logging.setFinishAt(new Date());

            incrementTaskLoggingRepository.save(logging);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            return false;
        }
        return true;
    }

    @Override
    public RespBody page(IncremTaskLoggingPageDto dto) {

        Pageable pageable = new PageRequest(dto.getPage() - 1, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        Page<IncrementTaskLogging> page = incrementTaskLoggingRepository.findAll((root, query, cb) -> {

            Predicate predicate = cb.and(root.isNotNull(), cb.equal(root.get("task").get("id").as(Long.class), dto.getTaskId()));

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

        PageQueryVo<IncremTaskLoggingPageVo> pageQueryVo = new PageQueryVo<>();

        SystemUtils.pageResultMapper(page, dto, pageQueryVo);

        pageQueryVo.setContent(
                page.getContent().stream().map(log -> {
                    IncremTaskLoggingPageVo vo = new IncremTaskLoggingPageVo();
                    Task task = log.getTask();
                    vo.setTaskId(task.getId());
                    vo.setTaskUuid(task.getUuid());
                    vo.setStartAt(log.getCreateAt());
                    vo.setAuto(log.isAuto());
                    vo.setSuccess(log.isSuccess());
                    long start = log.getCreateAt().getTime();
                    long end = log.getFinishAt().getTime();
                    vo.setSpendTime(SystemUtils.parse(end - start));
                    vo.setProcData(log.getProcData());
                    return vo;
                }).collect(Collectors.toList())
        );

        return new RespBody<>().setData(pageQueryVo);
    }
}
