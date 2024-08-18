package com.study.seckill.controller;

import com.study.seckill.common.base.BaseRequest;
import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.model.http.UserReq;
import com.study.seckill.model.http.UserResp;
import com.study.seckill.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 包名: com.study.seckill.controller
 * 类名: UserController
 * 创建用户: 25789
 * 创建日期: 2022年10月01日 19:08
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/getPhoneSmsCode")
    public BaseResponse getPhoneSmsCode(@Valid @RequestBody BaseRequest<UserReq.SensPhoneCodeReqInfo> req) {
        UserReq.SensPhoneCodeReqInfo reqData = req.getData();
        userService.getPhoneSmsCode(reqData);
        return BaseResponse.ok(true);
    }

    @PostMapping("/getEmailEmCode")
    public BaseResponse getEmailEmCode(@Valid @RequestBody BaseRequest<UserReq.SensEmailCodeReqInfo> req) {
        UserReq.SensEmailCodeReqInfo reqData = req.getData();
        userService.getEmailSmsCode(reqData);
        return BaseResponse.ok(true);
    }

    @PostMapping("/userPhoneLogin")
    public BaseResponse userPhoneLogin(@Valid @RequestBody BaseRequest<UserReq.LoginUserPhoneInfo> req) {
        UserReq.LoginUserPhoneInfo loginInfo = req.getData();
        String token = userService.userPhoneLogin(loginInfo.getPhone(), loginInfo.getSmsCode());
        UserResp.BaseUserResp resp = new UserResp.BaseUserResp();
        resp.setToken(token);
        return BaseResponse.ok(resp);
    }

    @PostMapping("/userPhoneRegister")
    public BaseResponse userPhoneRegister(@Valid @RequestBody BaseRequest<UserReq.LoginUserPhoneInfo> req) {
        UserReq.LoginUserPhoneInfo loginInfo = req.getData();
        userService.userPhoneRegister(loginInfo.getPhone(), loginInfo.getSmsCode());
        return BaseResponse.ok(true);
    }

    @PostMapping("/userEmailLogin")
    public BaseResponse userEmailLogin(@Valid @RequestBody BaseRequest<UserReq.LoginUserEmailInfo> req) {
        UserReq.LoginUserEmailInfo loginInfo = req.getData();
        String token = userService.userEmailLogin(loginInfo.getEmail(), loginInfo.getEmCode());
        UserResp.BaseUserResp resp = new UserResp.BaseUserResp();
        resp.setToken(token);
        return BaseResponse.ok(resp);
    }

    @PostMapping("/userEmailRegister")
    public BaseResponse userEmailRegister(@Valid @RequestBody BaseRequest<UserReq.LoginUserEmailInfo> req) {
        UserReq.LoginUserEmailInfo loginInfo = req.getData();
        userService.userEmailRegister(loginInfo.getEmail(), loginInfo.getEmCode());
        return BaseResponse.ok(true);
    }

    /**
     * 内部方法，根据token验证token是否合法，然后返回对应的user对象.
     * 内部系统没有社保号等那么多标识，所以一般请求参数就不使用BaseRequest的格式了.
     *
     * @param token
     * @param serviceId
     * @return
     */
    @PostMapping("/checkUserToken")
    public BaseResponse checkUserToken(String token, String serviceId) {
        return userService.checkUserToken(serviceId,token);
    }

    @PostMapping("/userLogout")
    public BaseResponse userLogout(HttpServletRequest request) {
        String token = request.getHeader("token");
        userService.userLogout(token);
        return BaseResponse.ok(true);
    }
}