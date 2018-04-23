package com.mrray.desens.task.entity.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mrray.desens.task.utils.AESUtils;
import com.mrray.desens.task.utils.String2ArrayConverter;
import io.swagger.annotations.ApiModel;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;

import java.util.List;

@ApiModel(description = "邮件添加模型")
public class EmailSaveDto {

    /**
     * 发送者
     */
    @NotBlank(message = "sender 不能为空")
    @Email(message = "sender 不是邮箱")
    private String sender;


    private String senderName;

    /**
     * 发送者邮箱密码
     */
    @NotBlank(message = "password 不能为空")
    private String password;

    /**
     * 邮件服务器地址
     */
    @NotBlank(message = "host 不能为空")
    private String host;

    private String protocol = "smtp";

    private Integer port = 25;

    private boolean enabled;

    /**
     * 邮件接受者
     */
    @JsonDeserialize(converter = String2ArrayConverter.class)
    private List<String> receiverAddress;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(List<String> receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void copy(com.mrray.desens.task.entity.domain.Email email) {
        // copy basic property
        BeanUtils.copyProperties(this, email);

        email.setPassword(AESUtils.encrypt("4cb2f0ce79a6cd45463aa86dbf0cac26", this.getPassword()));

    }

    public com.mrray.desens.task.entity.domain.Email covert() {

        com.mrray.desens.task.entity.domain.Email email = new com.mrray.desens.task.entity.domain.Email();

        // copy basic property
        BeanUtils.copyProperties(this, email);

        email.setPassword(AESUtils.encrypt("4cb2f0ce79a6cd45463aa86dbf0cac26", this.getPassword()));

        return email;
    }
}
