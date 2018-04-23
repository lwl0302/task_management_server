package com.mrray.desens.task.entity.domain;

import javax.persistence.*;

@Entity
@Table(name = "t_email_receiver")
public class EmailReceiver {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String receiver;

    @ManyToOne(targetEntity = Email.class)
    private Email sender;

    public EmailReceiver() {
    }

    public EmailReceiver(String receiver, Email sender) {
        this.receiver = receiver;
        this.sender = sender;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Email getSender() {
        return sender;
    }

    public void setSender(Email sender) {
        this.sender = sender;
    }


}
