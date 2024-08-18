package com.study.seckill.service;

import com.study.seckill.security.AdminUser;
import com.study.seckill.security.Token;

/**
 * 包名: com.study.seckill.service
 * 类名: AdminTokenService
 * 创建用户: 25789
 * 创建日期: 2022年10月10日 17:32
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
public interface AdminTokenService {
    AdminUser getLoginUser(String token);
    Token saveToken(AdminUser loginUser);
    void deleteToken(String token);
    void refresh(AdminUser loginUser);
}
