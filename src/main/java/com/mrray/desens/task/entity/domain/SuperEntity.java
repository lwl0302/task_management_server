package com.mrray.desens.task.entity.domain;

import org.apache.commons.lang.RandomStringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Arthur on 2017/7/20.
 */
@MappedSuperclass
public class SuperEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private String uuid = RandomStringUtils.random(8, true, true).toLowerCase();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_at")
    private Date createAt = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
