package com.study.seckill.security;

import com.alibaba.fastjson.JSONObject;
import com.study.seckill.common.entity.CommonWebUser;
import com.study.seckill.common.util.cache.RedisUtil;
import com.study.seckill.common.util.context.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 包名: com.study.seckill.security
 * 类名: WebUserUtil
 * 创建用户: 25789
 * 创建日期: 2022年10月01日 18:29
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
public class WebUserUtil {
    public static final String SESSION_WEB_USER_KEY = "web_user_key";
    public static CommonWebUser getLoginUser(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        HttpSession session = request.getSession();
        CommonWebUser commonWebUser = null;
        if (session.getAttribute(SESSION_WEB_USER_KEY) != null){
            commonWebUser = (CommonWebUser) session.getAttribute(SESSION_WEB_USER_KEY);
        }else {
            RedisUtil redisUtil = SpringContextHolder.getBean("redisUtil");
            if (StringUtils.isNotEmpty(request.getHeader("token"))) {
                Object object = redisUtil.get(request.getHeader("token"));
                if (object != null) {
                    commonWebUser = JSONObject.parseObject(object.toString(), CommonWebUser.class);
                    session.setAttribute(SESSION_WEB_USER_KEY, commonWebUser);
                }
            }

        }
        return commonWebUser;
    }
}