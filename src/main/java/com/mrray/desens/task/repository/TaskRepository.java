package com.mrray.desens.task.repository;

import com.mrray.desens.task.entity.domain.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by Arthur on 2017/7/20.
 */
public interface TaskRepository extends BaseRepository<Task> {

    Task findByUuid(String uuid);

    Task findByExtract(String uuid);

    Task findByDesensitive(String uuid);

    Task findByLoads(String uuid);

    Long countByStatusAndDeleted(int status, boolean deleted);

    @Query("select avg(cost) from Task")
    Long avgCost();

    @Query("select count(id) from Task where start between :a and :b")
    Long dayTasks(@Param("a") Date a, @Param("b") Date b);

    List<Task> findByPlusAndAuto(boolean pluse, boolean auto);

    List<Task> findByAuto(boolean auto);

    List<Task> findAllByStatus(int status);

    @Query(value = "select * from t_task where status=:status order by start limit :count", nativeQuery = true)
    List<Task> findByStatusOrderByStartLimit(@Param("status") int status, @Param("count") long count);
}
