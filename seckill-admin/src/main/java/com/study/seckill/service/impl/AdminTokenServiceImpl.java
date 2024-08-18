package com.study.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.study.seckill.security.AdminUser;
import com.study.seckill.security.Token;
import com.study.seckill.service.AdminTokenService;
import com.study.seckill.common.util.cache.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 包名: com.study.seckill.service.impl
 * 类名: AdminTokenServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年10月10日 17:32
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Service
public class AdminTokenServiceImpl implements AdminTokenService {
    public static final String key_token_user = "nft:a:t:u:%s";
    public static final String key_user_token = "nft:a:u:t:%s";
    @Value("${token.expire.seconds}")
    private Integer expireSeconds;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public AdminUser getLoginUser(String token) {
        Object value = redisUtil.get(String.format(key_token_user, token));
        if (value == null){
            return null;
        }
        return JSON.parseObject(value.toString(),AdminUser.class);
    }

    @Override
    public Token saveToken(AdminUser loginUser) {
        Object token = redisUtil.get(String.format(key_user_token, loginUser.getId()));
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
        loginUser.setToken(token.toString());
        refresh(loginUser);
        return new Token(token.toString(), loginUser.getLoginTime(), loginUser.getId());
    }

    @Override
    public void deleteToken(String token) {
        AdminUser loginUser = getLoginUser(token);
        if (loginUser != null) {
            String key = String.format(key_token_user, token);
            redisUtil.del(key);
            String ut = String.format(key_user_token, loginUser.getId());
            if (redisUtil.hasKey(ut)) {
                redisUtil.del(ut);
            }
        }
    }

    @Override
    public void refresh(AdminUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireSeconds * 1000);
        // 缓存
        String key = String.format(key_token_user, loginUser.getToken());
        redisUtil.setex(key, expireSeconds, JSON.toJSONString(loginUser));
        key = String.format(key_user_token, loginUser.getId());
        redisUtil.setex(key, expireSeconds, loginUser.getToken());
    }
}