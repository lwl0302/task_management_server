package com.mrray.desens.task.service.impl;

import com.mrray.desens.task.entity.domain.Email;
import com.mrray.desens.task.entity.domain.EmailReceiver;
import com.mrray.desens.task.entity.dto.EmailSaveDto;
import com.mrray.desens.task.entity.vo.EmailVo;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.repository.EmailReceiverRepository;
import com.mrray.desens.task.repository.EmailRepository;
import com.mrray.desens.task.service.EmailService;
import com.mrray.desens.task.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailRepository emailRepository;

    private final EmailReceiverRepository emailReceiverRepository;

    @Autowired
    public EmailServiceImpl(EmailRepository emailRepository, EmailReceiverRepository emailReceiverRepository) {
        this.emailRepository = emailRepository;
        this.emailReceiverRepository = emailReceiverRepository;
    }

    @Override
    public RespBody save(EmailSaveDto dto) {

        try {
            MessageUtil.testEmailConn(dto);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);

            return new RespBody<>().setStatus(HttpStatus.BAD_REQUEST).setMessage("登录失败，请检查登录参数。");
        }

        List<Email> emails = emailRepository.findAll();
        Email email;
        if (emails == null || emails.size() == 0) {
            email = dto.covert();
        } else {
            email = emails.get(0);
            dto.copy(email);
        }

        List<EmailReceiver> emailReceivers = email.getReceivers();
        List<String> receivers = dto.getReceiverAddress();
        List<EmailReceiver> temp = new ArrayList<>();
        for (EmailReceiver emailReceiver : emailReceivers) {
            String address = emailReceiver.getReceiver();
            if (receivers.contains(address)) {
                temp.add(emailReceiver);
            } else {
                emailReceiver.setSender(null);
                emailReceiverRepository.delete(emailReceiver);
            }
        }

        for (String receiver : receivers) {
            EmailReceiver emailReceiver = emailReceiverRepository.findByReceiver(receiver);
            if (emailReceiver == null) {
                emailReceiver = new EmailReceiver(receiver, email);
                temp.add(emailReceiver);
            }
        }

        emailRepository.save(email);

        emailReceiverRepository.save(temp);

        return new RespBody<>().setStatus(HttpStatus.CREATED).setMessage("添加成功");
    }

    @Override
    public RespBody find() {

        List<Email> emails = emailRepository.findAll();

        if (emails == null || emails.size() == 0) {
            return new RespBody<>().setMessage("未配置邮箱");
        }
        Email email = emails.get(0);

        if (email != null) {
            EmailVo vo = EmailVo.convert(email);

            return new RespBody<>().setData(vo);
        }

        return new RespBody<>().setMessage("未配置邮箱");
    }

    @Override
    public RespBody updateStatus() {
        List<Email> emails = emailRepository.findAll();

        if (emails == null || emails.size() == 0) {
            return new RespBody<>().setMessage("未配置邮箱");
        }

        Email email = emails.get(0);

        if (email != null) {
            email.setEnabled(!email.isEnabled());
            emailRepository.save(email);
        }

        return new RespBody<>();
    }
}
