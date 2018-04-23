package com.mrray.desens.task.entity.domain;

public enum TaskStatus {

    START("Start"), PAUSE("Pause"), FAILED("Failed"), FINISHED("Finished"), EXTRACT("Extract"), SCAN("Scan"), DESENS("Desens"), LOAD("Load"), CANCEL("Cancel");

    private String action;

    TaskStatus(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
