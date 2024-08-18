package com.study.seckill.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * 包名: com.study.seckill.security
 * 类名: SecurityConfig
 * 创建用户: 25789
 * 创建日期: 2022年10月10日 16:03
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    @Resource(name="authenticationSuccessHandler")
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    @Resource(name="formAuthenticationSuccessHandler")
    private AuthenticationSuccessHandler formAuthenticationSuccessHandler;
    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;
    @Autowired
    private TokenFilter tokenFilter;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);
    }
    /**
     * 1、配置的是认证信息, AuthenticationManagerBuilder 这个类,就是AuthenticationManager的建造者, 我们只需要向这个类中, 配置用户信息,
     *    就能生成对应的AuthenticationManager, 这个类也提过,是用户身份的管理者, 是认证的入口, 因此,我们需要通过这个配置,想security提供真实的用户身份。
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }
    /**
     * 2、配置Security的认证策略, 每个模块配置使用and结尾。这个也是最复杂的
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /**
         * 1\. HttpSecurity被声明为链式调用
         * 其中配置方法包括
         *  1\. authorizeRequests（）url拦截配置
         *  2\. formLogin（）表单验证
         *  3\. httpBasic（）表单验证
         *  4\. csrf（）提供的跨站请求伪造防护功能
         */
        /**
         * 2\. authorizeRequests目的是指定url进行拦截的，也就是默认这个url是“/”也就是所有的
         * anyanyRequest（）、antMatchers（）和regexMatchers（）三种方法来拼配系统的url。并指定安全策略
         */
        http.authorizeRequests()
                //这里指定什么样的接口地址的请求，需要什么样的权限 ANT模式的URL匹配器
                .antMatchers("/product/**").hasAuthority("ADMIN")//管理员可以有插入权限权限
                .antMatchers("/admin/**").hasAuthority("SUPERADMIN")//超级管理员才有赋权的权限
                .antMatchers("/login",  "/user/**", "/pub/**").permitAll();//标识list所有权限都可以直接访问，即使不登录也可以访问。一般将login页面放给这个权限一堆配置和逻辑代码
        http.authorizeRequests()
                .anyRequest()
                .authenticated();
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);
        http.cors();//启动跨域，防止前端多个域名的情况

        http.csrf().disable();

        http.logout().logoutUrl("/user/logout").logoutSuccessHandler(logoutSuccessHandler);

        // 基于token，所以不需要session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        /**
         * 将自定义的过滤器加入configure中
         */
        http.addFilterAt(jsonUsernamePasswordFilter(),
                UsernamePasswordAuthenticationFilter.class);
        http.addFilterAt(formUsernamePasswordFilter(),
                JsonUsernamePasswordFilter.class);
        http.addFilterBefore(tokenFilter, JsonUsernamePasswordFilter.class);

    }
    /**
     * 3、这个配置方法用于配置静态资源的处理方式，可使用 Ant 匹配规则。就是可以不用认证就可以直接访问的接口
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/static")
                .antMatchers("/commons");
    }

    /**
     * SpringSecurity5.X要求必须指定密码加密方式，否则会在请求认证的时候报错
     * 同样的，如果指定了加密方式，就必须您的密码在数据库中存储的是加密后的，才能比对成功     *
     * @return
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //登录的Filter
    @Bean
    public JsonUsernamePasswordFilter jsonUsernamePasswordFilter() throws Exception {
        JsonUsernamePasswordFilter filter = new JsonUsernamePasswordFilter();
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        filter.setFilterProcessesUrl("/user/login");
        //这句很关键，重用WebSecurityConfigurerAdapter配置的AuthenticationManager，不然要自己组装AuthenticationManager
        filter.setAuthenticationManager(authenticationManagerBean());
        return filter;
    }
    @Bean
    public FormUsernamePasswordFilter formUsernamePasswordFilter() throws Exception {
        FormUsernamePasswordFilter filter = new FormUsernamePasswordFilter();
        //jsp版本登录成功了我们要设置cookie,所以新写一个登录成功handler
        filter.setAuthenticationSuccessHandler(formAuthenticationSuccessHandler);
        //登录失败的暂时不做修改
        filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        filter.setFilterProcessesUrl("/user/loginPage");
        //这句很关键，重用WebSecurityConfigurerAdapter配置的AuthenticationManager，不然要自己组装AuthenticationManager
        filter.setAuthenticationManager(authenticationManagerBean());
        return filter;
    }
    //跨域相关代码
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("PUT", "DELETE", "GET", "POST", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setExposedHeaders(Arrays.asList("access-control-allow-headers",
                "access-control-allow-methods",
                "access-control-allow-origin",
                "access-control-max-age",
                "X-Frame-Options"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}