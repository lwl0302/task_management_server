package com.mrray.desens.task.entity.vo;

/**
 * Created by ln on 2017/8/8.
 */
public class LoadRespVo {

    private String mainTaskId;

    private String loadId;

    private Long count;

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

    public String getLoadId() {
        return loadId;
    }

    public void setLoadId(String loadId) {
        this.loadId = loadId;
    }
}
