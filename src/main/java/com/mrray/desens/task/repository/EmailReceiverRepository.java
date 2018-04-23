package com.mrray.desens.task.repository;

import com.mrray.desens.task.entity.domain.EmailReceiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmailReceiverRepository extends JpaRepository<EmailReceiver, Long>, JpaSpecificationExecutor<EmailReceiver> {

    EmailReceiver findByReceiver(String receiver);

}
