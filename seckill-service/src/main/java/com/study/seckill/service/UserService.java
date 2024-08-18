package com.study.seckill.service;

import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.model.http.UserReq;

/**
 * 包名: com.study.seckill.service
 * 类名: UserService
 * 创建用户: 25789
 * 创建日期: 2022年09月28日 22:53
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
public interface UserService {
    String userPhoneLogin(String phone,String smsCode);
    String userEmailLogin(String email,String emCode);

    void userPhoneRegister(String phone, String smsCode);

    void userEmailRegister(String email, String emCode);

    void getPhoneSmsCode(UserReq.SensPhoneCodeReqInfo info);

    void getEmailSmsCode(UserReq.SensEmailCodeReqInfo info);

    void userLogout(String token);

    BaseResponse checkUserToken(String serviceId, String token);
}
