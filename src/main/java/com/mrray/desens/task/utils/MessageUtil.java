package com.mrray.desens.task.utils;

import com.mrray.desens.task.entity.domain.Email;
import com.mrray.desens.task.entity.domain.EmailReceiver;
import com.mrray.desens.task.entity.dto.EmailSaveDto;
import com.mrray.desens.task.repository.EmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class MessageUtil {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);

    public static void testEmailConn(EmailSaveDto email) throws MessagingException {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        sender.setDefaultEncoding("UTF-8");
        sender.setUsername(email.getSender());
        sender.setPassword(email.getPassword());
        sender.setHost(email.getHost());
        sender.setPort(email.getPort());
        sender.testConnection();
    }

    /**
     * 发送邮件
     *
     * @param emailRepository
     * @param content
     * @return
     */
    public static boolean sendEmail(EmailRepository emailRepository, String content) {

        List<Email> all = emailRepository.findAll();
        if (all == null || all.size() == 0) {
            return false;
        }
        Email email = all.get(0);
        if (!email.isEnabled()) {
            return false;
        }

        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "false");

            sender.setDefaultEncoding("UTF-8");
            sender.setUsername(email.getSender());
            sender.setPassword(AESUtils.decrypt("4cb2f0ce79a6cd45463aa86dbf0cac26", email.getPassword()));
            sender.setHost(email.getHost());
            sender.setPort(email.getPort());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("系统通知");
            String senderName = MimeUtility.encodeText(email.getSenderName());
            String from = String.format("%s<%s>", senderName, sender.getUsername());
            message.setFrom(from);
            List<String> list = email.getReceivers().stream().map(EmailReceiver::getReceiver).collect(Collectors.toList());
            String[] receivers = new String[list.size()];
            message.setTo(list.toArray(receivers));
            message.setText(content);
            // 发送邮件
            sender.send(message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    public static String getTaskJumpMessage(Long id) {
        return String.format("任务ID#%s 由于前一次任务还未完成，本次自动执行将自动跳过，请知晓。", id);
    }

    public static String getTaskFailedMessage(Long id, String step, String reason) {
        return String.format("任务ID#%s %s失败，失败原因为：%s，请知晓。", id, step, reason);
    }



}
