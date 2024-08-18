package com.study.seckill.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 包名: com.study.seckill.security
 * 类名: AdminUserUtil
 * 创建用户: 25789
 * 创建日期: 2022年10月10日 16:02
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
public class AdminUserUtil {
    /**
     * 获取当前用户
     *
     * @return
     */
    public static AdminUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication instanceof AnonymousAuthenticationToken) {
                return null;
            }
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                return (AdminUser) authentication.getPrincipal();
            }
        }
        return null;
    }

}