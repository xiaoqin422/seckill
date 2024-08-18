package com.study.seckill.common.util.sdk;

import com.study.seckill.common.entity.EmailTemplate;
import com.study.seckill.common.exception.SecKillException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 包名: com.study.seckill.util
 * 类名: EmailUtil
 * 创建用户: 25789
 * 创建日期: 2022年10月01日 19:56
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Component
@Slf4j
public class EmailUtil {
    @Resource
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailTemplate emailTemplate;
    /**
     * 发件人邮箱
     */
    @Value("${email.system}")
    private String mailbox;

    public void sendEmail(String to,String subject,String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        send(message);
    }

    private void send(SimpleMailMessage mailMessage) {
        try {
            mailMessage.setFrom(mailbox);
            //抄送
            // mailMessage.setCc(mailbox);
            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            log.error("发送失败！发送人:{}", mailMessage.getTo().toString());
            throw new SecKillException("邮件服务异常",e);
        }
    }

    /**
     * @param name 姓名
     * @param to   接收人邮箱
     * @param title    标题
     * @param code     验证码
     * @param deptName 部门
     */
    public void sendMultipartEmail(String name ,String to, String title, String code, String deptName) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            //true表示需要创建一个multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailbox);
            helper.setTo(to);//邮件接收者
            helper.setSubject(title);//邮件主题
            String email = emailTemplate.getHtml(title, name, deptName ,"账号注册", code);
            helper.setText(email, true);//邮件内容
            javaMailSender.send(message);
            log.info(to + "发送成功。");
        } catch (MessagingException e) {
            log.error("发送失败！收件人:{}。err:{}", to,e.getMessage());
            throw new SecKillException("邮件服务异常",e);
        }
    }
}