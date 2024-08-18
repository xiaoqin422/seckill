package com.study.seckill.common.entity;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {
    /**
     * @param title 标题
     * @param userName 用户名
     * @param type 类型 【邮箱验证】
     * @param captcha 【验证码】
     * @param name 姓名
     * @return
     */
    public String getHtml(String title, String name, String userName, String type, String captcha) {
        String emailTemplate = System.getProperty("emailTemplate");
        emailTemplate = emailTemplate.replace("$(title)", title);
        emailTemplate = emailTemplate.replace("$(name)", name);
        emailTemplate = emailTemplate.replace("$(userName)", userName);
        emailTemplate = emailTemplate.replace("$(type)", type);
        emailTemplate = emailTemplate.replace("$(captcha)", captcha);
        return emailTemplate;
    }
}