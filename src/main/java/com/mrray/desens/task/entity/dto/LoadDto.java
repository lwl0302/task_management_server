package com.mrray.desens.task.entity.dto;

/**
 * Created by ln on 2017/7/20.
 */
public class LoadDto {
    private String mainTaskId;
    private SourceInfoDto sourceInfo;
    private String extractId;
    private TargetInfoDto targetInfo;

    public String getMainTaskId() {
        return mainTaskId;
    }

    public void setMainTaskId(String mainTaskId) {
        this.mainTaskId = mainTaskId;
    }

    public SourceInfoDto getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(SourceInfoDto sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public String getExtractId() {
        return extractId;
    }

    public void setExtractId(String extractId) {
        this.extractId = extractId;
    }

    public TargetInfoDto getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfoDto targetInfo) {
        this.targetInfo = targetInfo;
    }
}
