package com.study.seckill.common.exception;

/**
 *
 */
public enum ErrorMessage {

    /**
     * 通用ERROR
     */
    SYS_ERROR(10000, "系统开小差了,稍后再试"),
    PARAM_ERROR(10001, "参数错误"),
    SMSCODE_SEND_ERROR(16002, "验证码发送错误"),
    SMSCODE_PINCI_ERROR(16003,"验证码频次过高"),
    SMSCODE_VISIT_ERROR(16004,"验证码请求超出限制"),
    STOCK_NOT_ENOUGH(20001, "库存不足！"),
    REPEAT_ORDER_ERROR(20002, "不能重复下单！"),
    SECKILL_NOT_START(20003, "秒杀活动还没开始！"),
    SECKILL_FAILED(20004, "秒杀失败！！"),
    SECKILL_RATE_LIMIT_ERROR(20005, "你被限流了！直接返回失败！"),
    SECKILL_VALIDATE_ERROR(20006, "秒杀验证码校验失败"),
    SECKILL_USER_VISIT_LIMIT_ERROR(20007, "用户访问过于频繁"),
    /**
     * user相关error.
     */
    SMSCODE_ERROR(30001, "短信验证码错误"),
    EMCODE_ERROR(30002, "邮箱验证码错误"),

    PHONE_EXIST(30003, "手机号已经存在"),
    PHONE_NO_EXIST(30004, "手机号未绑定"),
    EMAIL_EXIST(30005, "邮箱已经存在"),
    EMAIL_NO_EXIST(30006, "邮箱未绑定"),
    USER_NEED_LOGIN(30007, "用户需要登录"),
    TOKEN_CHECK_SERVICEID_ERROR(18001, "serviceId错误"),
    TOKEN_CHECK_IP_ERROR(18002, "IP错误，不允许使用"),
    TOKEN_CHECK_INVALID_ERROR(18003, "token失效或不存在"),
    IP_LOC_ERROR(19001, "ip范围错误，不允许登录"),
    /**
     * admin的login相关参数.
     */
    NO_JSON(-8887, "Json format error!"),
    NO_LOGIN(-9999, "Please login first!"),
    USER_NOT_EXISTS(-9998, "User does not exist!"),
    USER_NO_PERMISSION(-9997, "User does not have permission!"),
    LOGIN_ERROR(-9996, "Login error!"),
    LOGIN_ERROR_USERNAME_PASSWORD_ERROR(-9991, "Username or password error!"),
    LOGIN_ERROR_PASSWORD_ERROR(-9990, "Password error!"),
    ACCOUNT_DISABLE(-9989, "Account disable!"), ;


    private Integer code;
    private String message;

    ErrorMessage(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public ErrorMessage setMessage(String message) {
        this.message = message;
        return this;
    }
}
