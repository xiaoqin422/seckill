package com.study.seckill.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.common.entity.CommonWebUser;
import com.study.seckill.common.exception.ErrorMessage;
import com.study.seckill.common.util.cache.RedisUtil;
import com.study.seckill.filter.SsoLoginFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 包名: com.study.seckill.security
 * 类名: UserLoginFilter
 * 创建用户: 25789
 * 创建日期: 2022年10月01日 18:23
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Slf4j
@WebFilter(filterName = "userLoginFilter",urlPatterns = "/*")
public class UserLoginFilter extends SsoLoginFilter {
    @Value("${login.filter.pattern}")
    private String urlPatternStr;
    @Value("${login.filter.token.url}")
    private String tokenCheckUrlStr;
    @Value("${login.filter.serviceId}")
    private String serviceIdStr;
    @Override
    public void dealFilterParams() {
        this.urlPattern = urlPatternStr;
        this.tokenCheckUrl = tokenCheckUrlStr;
        this.serviceId = serviceIdStr;
    }
    @Override
    public void dealSessionItem(HttpServletRequest request, CommonWebUser commonWebUser) {
        HttpSession session = request.getSession();
        //session设置自己系统的专属信息
        session.setAttribute("newUser", commonWebUser);
    }
    @Override
    public void doLoginFailed(HttpServletRequest request, HttpServletResponse response, String type) {
        //设置自己的专属错误信息
        JSONObject json = new JSONObject();
        json.put("code","1660");
        json.put("message", "need Login, type:" + type);
        try {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSON.toJSONString(json));
            response.getWriter().close();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    @Override
    public String getUserToken(HttpServletRequest request, HttpServletResponse response) {
        //约定的是从header中获取token
        return request.getHeader("token");
    }
}