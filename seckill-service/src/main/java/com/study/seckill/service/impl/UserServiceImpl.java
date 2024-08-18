package com.study.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.common.base.Constant;
import com.study.seckill.common.entity.CommonWebUser;
import com.study.seckill.common.exception.ErrorMessage;
import com.study.seckill.common.exception.SecKillException;
import com.study.seckill.common.util.cache.RedisCounterRepository;
import com.study.seckill.dao.SsoUserDao;
import com.study.seckill.model.SeckillUser;
import com.study.seckill.model.SsoUser;
import com.study.seckill.model.http.UserReq;
import com.study.seckill.service.UserService;
import com.study.seckill.common.util.sdk.EmailUtil;
import com.study.seckill.common.util.cache.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 包名: com.study.seckill.service.impl
 * 类名: UserServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年09月28日 22:53
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private SsoUserDao userDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private RedisCounterRepository repository;

    @Override
    public String userPhoneLogin(String phone, String smsCode) {
        SsoUser user = userDao.selectByPhone(phone);
        if (user == null) {
            throw new SecKillException(ErrorMessage.PHONE_NO_EXIST.getMessage(), ErrorMessage.PHONE_NO_EXIST.getCode());
        }
        String key = String.format(Constant.redisKey.USER_PHONE_CODE, phone);
        Object existObject = redisUtil.get(key);
        String token = "";
        if (existObject == null || !existObject.equals(smsCode)) {
            throw new SecKillException(ErrorMessage.SMSCODE_ERROR.getMessage());
        } else {
            redisUtil.del(key + phone);
            CommonWebUser commonWebUser = new CommonWebUser();
            BeanUtil.copyProperties(user, commonWebUser);
            token = UUID.randomUUID().toString().replaceAll("-", "");
            redisUtil.set(token, JSON.toJSONString(commonWebUser), 60 * 60 * 24 * 30);
        }
        return token;
    }

    @Override
    public String userEmailLogin(String email, String emCode) {
        SsoUser user = userDao.selectByEmail(email);
        if (user == null) {
            throw new SecKillException(ErrorMessage.EMAIL_NO_EXIST.getMessage(), ErrorMessage.EMAIL_NO_EXIST.getCode());
        }
        String key = String.format(Constant.redisKey.USER_EMAIL_CODE, email);
        Object existObject = redisUtil.get(key);
        String token = "";
        if (existObject == null || !existObject.equals(emCode)) {
            throw new SecKillException(ErrorMessage.SMSCODE_ERROR.getMessage());
        } else {
            redisUtil.del(key + email);
            CommonWebUser commonWebUser = new CommonWebUser();
            BeanUtil.copyProperties(user, commonWebUser);
            token = UUID.randomUUID().toString().replaceAll("-", "");
            redisUtil.set(token, JSON.toJSONString(commonWebUser), 60 * 60 * 24 * 30);
        }
        return token;
    }

    @Override
    public void userPhoneRegister(String phone, String smsCode) {
        String key = String.format(Constant.redisKey.USER_PHONE_CODE, phone);
        Object object = redisUtil.get(key);
        if (object == null || !smsCode.equals(String.valueOf(object))) {
            throw new SecKillException(ErrorMessage.SMSCODE_ERROR.getMessage(), ErrorMessage.SMSCODE_ERROR.getCode());
        }
        SsoUser user = new SsoUser();
        user.setPhone(phone);
        user.setExternalId(repository.getUserForeignId());
        uniqueInsert(user);
        redisUtil.del(key);
    }

    @Override
    public void userEmailRegister(String email, String emCode) {
        String key = String.format(Constant.redisKey.USER_EMAIL_CODE, email);
        Object object = redisUtil.get(key);
        if (object == null || !emCode.equals(String.valueOf(object))) {
            throw new SecKillException(ErrorMessage.EMCODE_ERROR.getMessage(), ErrorMessage.EMCODE_ERROR.getCode());
        }
        SsoUser user = new SsoUser();
        user.setEmail(email);
        user.setExternalId(repository.getUserForeignId());
        uniqueInsert(user);
        redisUtil.del(key);
    }

    private void uniqueInsert(SsoUser record) {
        try {
            Assert.notNull(record.getExternalId(), "外部id不能为空!");
            record.setCreateTime(new Date());
            record.setIsDeleted(0);
            record.setIsEnabled(0);
            userDao.insert(record);
        } catch (Exception ex) {
            if (ex.getMessage().toLowerCase().indexOf("Duplicate entry") < 0) {
                throw new SecKillException(ex.getMessage());
            }
        }
    }

    @Override
    public void getPhoneSmsCode(UserReq.SensPhoneCodeReqInfo info) {
        Assert.notNull(info, "用户请求不为null");
        String phone = info.getPhone();
        log.info("===[手机频次限制]===");
        //频次验证, 例如30秒才能调用一次
        String visitKey = String.format(Constant.redisKey.USER_PHONE_CODE_PINCI, phone);
        long visitCount = redisUtil.incr(visitKey, 1L);
        redisUtil.expire(visitKey, 30);
        if (visitCount > 1) {
            throw new SecKillException(ErrorMessage.SMSCODE_PINCI_ERROR.getMessage(), ErrorMessage.SMSCODE_PINCI_ERROR.getCode());
        }
        log.info("===[手机频次限制合法]===");
        log.info("===[手机请求次数限制]===");
        visitKey = String.format(Constant.redisKey.USER_PHONE_CODE_VISIT, phone);
        visitCount = redisUtil.incr(visitKey, 1L);
        //过期时间设置1天
        redisUtil.expire(visitKey, TimeUnit.DAYS.toSeconds(1));
        if (visitCount > Constant.CODE_LIMIT) {
            throw new SecKillException(ErrorMessage.SMSCODE_VISIT_ERROR.getMessage(), ErrorMessage.SMSCODE_VISIT_ERROR.getCode());
        }
        log.info("===[手机请求次数限制合法]===");
        Integer type = info.getType();
        String key = String.format(Constant.redisKey.USER_PHONE_CODE, phone);
        //登录失败
        if (1 != type && userDao.selectByPhone(phone) == null) {
            throw new SecKillException(ErrorMessage.PHONE_NO_EXIST.getMessage());
        }
        redisUtil.set(key, "123456", 60 * 3);
    }

    @Override
    public void getEmailSmsCode(UserReq.SensEmailCodeReqInfo info) {
        Assert.notNull(info, "用户请求不为null");
        String email = info.getEmail();
        log.info("===[邮箱频次限制]===");
        //频次验证, 例如30秒才能调用一次
        String visitKey = String.format(Constant.redisKey.USER_EMAIL_CODE_PINCI, email);
        long visitCount = redisUtil.incr(visitKey, 1L);
        redisUtil.expire(visitKey, 30);
        if (visitCount > 1) {
            throw new SecKillException(ErrorMessage.SMSCODE_PINCI_ERROR.getMessage(), ErrorMessage.SMSCODE_PINCI_ERROR.getCode());
        }
        log.info("===[邮箱频次限制合法]===");
        log.info("===[邮箱请求次数限制]===");
        visitKey = String.format(Constant.redisKey.USER_EMAIL_CODE_VISIT, email);
        visitCount = redisUtil.incr(visitKey, 1L);
        //过期时间设置1天
        redisUtil.expire(visitKey, TimeUnit.DAYS.toSeconds(1));
        if (visitCount > Constant.CODE_LIMIT) {
            throw new SecKillException(ErrorMessage.SMSCODE_VISIT_ERROR.getMessage(), ErrorMessage.SMSCODE_VISIT_ERROR.getCode());
        }
        log.info("===[邮箱请求次数限制合法]===");
        String key = String.format(Constant.redisKey.USER_EMAIL_CODE, email);
        Integer type = info.getType();
        //登录失败
        if (1 != type && userDao.selectByEmail(email) == null) {
            throw new SecKillException(ErrorMessage.EMAIL_NO_EXIST.getMessage());
        }
        String code = RandomUtil.randomNumbers(6);
        emailUtil.sendEmail(email, "秒杀系统登录认证", "您的登录验证码为：" + code + "。有效期3分钟，请勿泄露给他人！");
        // emailUtil.sendMultipartEmail(seckillUser.getName(),email,"秒杀系统登录验证码",code,"系统公告");
        redisUtil.set(key, code, 60 * 3);
    }

    @Override
    public void userLogout(String token) {
        if (StringUtils.isNotEmpty(token)) {
            if (redisUtil.get(token) != null) redisUtil.del(token);
        }
    }

    @Override
    public BaseResponse checkUserToken(String serviceId, String token) {
        //验证serviceId是否合法，这个一般是sso系统给相关系统划分的，从数据库验证就行，此处知道相应逻辑就行
        checkServiceId(serviceId);
        if (StringUtils.isEmpty(token)) {
            throw new SecKillException(ErrorMessage.TOKEN_CHECK_INVALID_ERROR.getMessage(),
                    ErrorMessage.TOKEN_CHECK_INVALID_ERROR.getCode());
        }
        Object object = redisUtil.get(token);
        if (object == null) {
            throw new SecKillException(ErrorMessage.TOKEN_CHECK_INVALID_ERROR.getMessage(),
                    ErrorMessage.TOKEN_CHECK_INVALID_ERROR.getCode());
        }
        CommonWebUser commonWebUser = JSON.parseObject(object.toString(), CommonWebUser.class);
        return BaseResponse.ok(commonWebUser);
    }

    private void checkServiceId(String serviceId) {
        if (StringUtils.isBlank(serviceId) || !"service1".equals(serviceId)) {
            throw new SecKillException(ErrorMessage.TOKEN_CHECK_SERVICEID_ERROR.getMessage(),
                    ErrorMessage.TOKEN_CHECK_SERVICEID_ERROR.getCode());
        }
    }

}