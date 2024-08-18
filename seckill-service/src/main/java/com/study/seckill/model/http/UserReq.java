package com.study.seckill.model.http;

import com.study.seckill.model.vo.ImageCodeModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 包名: com.study.seckill.model.http
 * 类名: UserReq
 * 创建用户: 25789
 * 创建日期: 2022年10月01日 18:10
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Data
@EqualsAndHashCode
public class UserReq implements Serializable {
    @Data
    @EqualsAndHashCode
    public static class BasePhoneUserInfo implements Serializable{
        @NotBlank(message = "手机号不能为空")
        private String phone;
    }
    @Data
    @EqualsAndHashCode
    public static class BaseEmailUserInfo implements Serializable{
        @NotBlank(message = "邮箱不能为空")
        private String email;
    }
    @Data
    @EqualsAndHashCode
    public static class LoginUserPhoneInfo extends BasePhoneUserInfo {
        @NotBlank(message = "短信验证码不能为空")
        private String smsCode;
    }
    @Data
    @EqualsAndHashCode
    public static class LoginUserEmailInfo extends BaseEmailUserInfo {
        @NotBlank(message = "邮箱验证码不能为空")
        private String emCode;
    }
    @Data
    @EqualsAndHashCode
    public static class SensPhoneCodeReqInfo extends BasePhoneUserInfo {
        @NotNull(message = "验证码类型不能为空, 1注册 6登录")
        private Integer type;
        private String nvcVal;
    }
    @Data
    @EqualsAndHashCode
    public static class SensEmailCodeReqInfo extends BaseEmailUserInfo {
        @NotNull(message = "验证码类型不能为空, 1注册 6登录")
        private Integer type;
        private String nvcVal;
    }
}