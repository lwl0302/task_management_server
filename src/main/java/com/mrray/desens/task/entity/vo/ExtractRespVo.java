package com.mrray.desens.task.entity.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ln on 2017/8/8.
 */
public class ExtractRespVo extends BaseResourceInfoVo {

    private String mainTaskId;
    private List<String> tableNames = new ArrayList<>();
    private String extractId;
    private Long count;
    private Long intPlus;
    private Date datePlus;
    private boolean stop;//抽取任务的时候标记是否停止下一次抽取

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public String getMainTaskId() {
        return mainTaskId;
    }

    public void setMainTaskId(String mainTaskId) {
        this.mainTaskId = mainTaskId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getIntPlus() {
        return intPlus;
    }

    public void setIntPlus(Long intPlus) {
        this.intPlus = intPlus;
    }

    public Date getDatePlus() {
        return datePlus;
    }

    public void setDatePlus(Date datePlus) {
        this.datePlus = datePlus;
    }

    public String getExtractId() {
        return extractId;
    }

    public void setExtractId(String extractId) {
        this.extractId = extractId;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }
}
