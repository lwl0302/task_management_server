package com.mrray.desens.task.entity.dto;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * Created by Arthur on 2017/7/24.
 */
public class TaskControlDto {

    @NotEmpty(message = "task id must not be empty")
    @Length(min = 8, max = 8, message = "uuid only 8 chars")
    private String uuid;

    @NotNull(message = "op must not be null")
    @NotBlank(message = "op must not be blank")
    private String op;

    @AssertTrue(message = "op must be in start、pause、cancel")
    private boolean isValid() {
        return !StringUtils.isEmpty(op)&&Arrays.asList("start", "pause", "cancel").contains(this.op.toLowerCase());
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }
}
