package com.mrray.desens.task.entity.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_email")
public class Email extends SuperEntity {

    private String sender;

    // 发送人的名字
    private String senderName;

    private String password;

    private String host;

    private String protocol;

    private Integer port;

    private boolean enabled;

    @OneToMany(targetEntity = EmailReceiver.class, mappedBy = "sender", cascade = CascadeType.PERSIST)
    private List<EmailReceiver> receivers = new ArrayList<>();

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

    public List<EmailReceiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<EmailReceiver> receivers) {
        this.receivers = receivers;
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
}
