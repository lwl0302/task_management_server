package com.mrray.desens.task.entity.dto;

import org.hibernate.validator.constraints.NotBlank;

public class AppLicenseDto {

    @NotBlank(message = "serial can't be blank")
    private String serial;

    @NotBlank(message = "license can't be blank")
    private String license;

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
