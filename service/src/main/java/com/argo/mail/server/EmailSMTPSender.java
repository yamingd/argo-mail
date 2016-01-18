package com.argo.mail.server;

import com.argo.mail.EmailMessage;
import com.argo.mail.template.EmailTemplateFactory;
import com.argo.mail.MailServiceConfig;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yaming_deng on 14-8-27.
 */
@Component
public class EmailSMTPSender implements InitializingBean {

    public static final String UTF_8 = "UTF-8";
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_SUCCESS = "success";

    private JavaMailSenderImpl mailSender = null;

    @Autowired
    private EmailTemplateFactory templateFactory;

    @Override
    public void afterPropertiesSet() throws Exception {

        MailServiceConfig.load();

        mailSender = new JavaMailSenderImpl();

        mailSender.setDefaultEncoding(UTF_8);
        mailSender.setHost(MailServiceConfig.instance.getHost());
        mailSender.setUsername(MailServiceConfig.instance.getUser());
        mailSender.setPassword(MailServiceConfig.instance.getPasswd());
        mailSender.setPort(MailServiceConfig.instance.getPort());

        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", MailServiceConfig.instance.isAuth() + "");
        props.setProperty("mail.smtp.timeout", MailServiceConfig.instance.getTimeout() + "");

        Map<String, String> map = MailServiceConfig.instance.getProps();
        if (null != map) {
            props.putAll(map);
        }

        mailSender.setJavaMailProperties(props);

        logger.info("Mail Server. host={}, port={}, user={}", MailServiceConfig.instance.getHost(), MailServiceConfig.instance.getPort(), MailServiceConfig.instance.getUser());
    }

    public boolean send(EmailMessage emailMessage) {

        Preconditions.checkNotNull(emailMessage.title, "emailMessage.title should not be null");

        if (StringUtils.isBlank(emailMessage.body)){
            Preconditions.checkNotNull(emailMessage.bodyTemplateId, "emailMessage.bodyTemplateId should not be null.");
            try {
                String body = templateFactory.render(emailMessage);
                emailMessage.body = body;
            } catch (Exception e) {
                logger.error("邮件模板处理错误! 邮件为: " + emailMessage.id + ", bodyTemplateId=" + emailMessage.bodyTemplateId, e);
                return false;
            }
        }

        try {

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper message = null;
            if (emailMessage.attachments != null && emailMessage.attachments.size() > 0) {
                message = new MimeMessageHelper(msg, true, UTF_8);
            }else{
                message = new MimeMessageHelper(msg, false, UTF_8);
            }

            message.setFrom(emailMessage.fromAddress);
            message.setSubject(emailMessage.title);
            message.setTo(emailMessage.toAddress.toArray(new String[0]));

            if (emailMessage.ccAddress != null && emailMessage.ccAddress.size() > 0){
                message.setCc(emailMessage.ccAddress.toArray(new String[0]));
            }
            if (emailMessage.bccAddress != null && emailMessage.bccAddress.size() > 0){
                message.setBcc(emailMessage.bccAddress.toArray(new String[0]));
            }

            message.setText(emailMessage.body, emailMessage.format.equalsIgnoreCase("html"));

            if (emailMessage.attachments != null) {
                Iterator<String> itor = emailMessage.attachments.keySet().iterator();
                while (itor.hasNext()){
                    String name = itor.next();
                    String filePath = emailMessage.attachments.get(name);
                    message.addAttachment(name, new File(filePath));
                }
            }

            mailSender.send(msg);

        } catch (MessagingException e) {
            logger.error("邮件信息导常! 邮件为: " + emailMessage.id, e);
            //MetricCollectorImpl.current().markMeter(this.getClass(), STATUS_FAILED);
            return false;
        } catch (MailException me) {
            logger.warn("发送邮件失败! 邮件为: " + emailMessage.id, me);
            //MetricCollectorImpl.current().markMeter(this.getClass(), STATUS_FAILED);
            return false;
        }
        if (logger.isDebugEnabled()){
            logger.debug("邮件发送成功. id=" + emailMessage.id);
        }

        //MetricCollectorImpl.current().markMeter(this.getClass(), STATUS_SUCCESS);

        return true;
    }

}
