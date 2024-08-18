package com.study.seckill.security;

import com.study.seckill.common.util.CookieUtils;
import com.study.seckill.service.AdminTokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 包名: com.study.seckill.security
 * 类名: TOkenFilter
 * 创建用户: 25789
 * 创建日期: 2022年10月10日 16:03
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Component
@Slf4j
public class TokenFilter extends OncePerRequestFilter {
    private static final String TOKEN_KEY = "token";
    @Autowired
    private AdminTokenService tokenService;
    @Autowired
    private MyUserDetailsService userDetailsService;

    private static final Long MINUTES_30 = 30 * 60 * 1000L;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 跟Filter类似的逻辑，根据http请求的header数据中的token进行验证并组装对象，认证失败不做相应处理，Security自己会做失败处理
        String token = getToken(request);
        if (StringUtils.isNotBlank(token)) {
            AdminUser loginUser = tokenService.getLoginUser(token);
            if (loginUser != null) {
                loginUser = checkLoginTime(loginUser);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser,
                        null, loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
    /**
     * 校验时间<br>
     * 过期时间与当前时间对比，临近过期30分钟内的话，自动刷新缓存
     *
     * @param loginUser
     * @return
     */
    private AdminUser checkLoginTime(AdminUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MINUTES_30) {
            String token = loginUser.getToken();
            loginUser = (AdminUser) userDetailsService.loadUserByUsername(loginUser.getUsername());
            loginUser.setToken(token);
            tokenService.refresh(loginUser);
        }
        return loginUser;
    }
    /**
     * 根据参数或者header获取token
     *
     * @param request
     * @return
     */
    public static String getToken(HttpServletRequest request) {
        String token = request.getParameter(TOKEN_KEY);
        if (StringUtils.isBlank(token)) {
            token = request.getHeader(TOKEN_KEY);
            if (StringUtils.isBlank(token)) {
                token = CookieUtils.getValue(request, TOKEN_KEY);
            }
        }
        return token;
    }
}