package com.mrray.desens.task.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mrray.desens.task.entity.domain.Email;
import com.mrray.desens.task.entity.domain.EmailReceiver;
import com.mrray.desens.task.utils.AESUtils;
import com.mrray.desens.task.utils.Array2StringConverter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class EmailVo {

    private String sender;

    private String senderName;

    private String password;

    private String host;

    private String protocol;

    private Integer port = 25;

    private boolean enabled;

    @JsonSerialize(converter = Array2StringConverter.class)
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

    public static EmailVo convert(Email email) {
        EmailVo vo = new EmailVo();

        // BeanUtils copy basic property
        BeanUtils.copyProperties(email, vo);
        vo.setPassword(AESUtils.decrypt("4cb2f0ce79a6cd45463aa86dbf0cac26", email.getPassword()));

        vo.setReceiverAddress(
                email.getReceivers().stream().map(EmailReceiver::getReceiver).collect(Collectors.toList())
        );

        return vo;
    }
}
