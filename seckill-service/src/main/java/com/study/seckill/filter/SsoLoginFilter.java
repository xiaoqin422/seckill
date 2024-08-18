package com.study.seckill.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.study.seckill.common.entity.CommonWebUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
@Slf4j
public abstract class SsoLoginFilter implements Filter {
    protected static String SESSION_WEB_KEY = "sso_session_web_key";
    //权限验证的基础参数
    protected String urlPattern;
    protected String tokenCheckUrl;
    protected String serviceId;
    public void init(FilterConfig filterConfig) throws ServletException {
        //设置filter相关的params，包括urlPattern、tokenCheckUrl和serviceId
        dealFilterParams();
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        HttpSession session = request.getSession();
        String url = request.getRequestURI();
        log.info("url:=" +url+ ",pattern:="+urlPattern);
        if (url.matches(urlPattern)) {
            if (session.getAttribute(SESSION_WEB_KEY) != null) {
                log.info("session has value");
                filterChain.doFilter(request, response);
                return;
            } else {
                //获取token相关信息
                String token = getUserToken(request, response);
                log.info("session has not value, has token:="+ token);
                if (StringUtils.isNotEmpty(token)) {
                    CommonWebUser commonWebUser = getUserFromSsoCenter(token);
                    if (commonWebUser != null) {
                        session.setAttribute(SESSION_WEB_KEY, commonWebUser);
                        //设置其他session信息
                        dealSessionItem(request, commonWebUser);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    //token验证失败，需要再次处理
                    doLoginFailed(request,response, "2");
                } else {
                    //token不存在
                    doLoginFailed(request,response, "1");
                }
                return;
            }
        }
        filterChain.doFilter(request, response);
        return;
    }
    //设置urlPattern、tokenCheckUrl和serviceId的内容
    public abstract void dealFilterParams();
    //相关方法，需要调用方根据内部决定是否实现，用于session内容再次实现
    public abstract void dealSessionItem(HttpServletRequest request, CommonWebUser commonWebUser);
    //权限失败后的相关方法，需要调用方进行相应处理
    public abstract void doLoginFailed(HttpServletRequest request, HttpServletResponse response, String type);
    //获取token的方法，需要调用方进行相应处理
    public abstract String getUserToken(HttpServletRequest request, HttpServletResponse response);
    //如果要换其他实现方式，可以子方法中重写此方法，可以不使用httpClient
    public CommonWebUser getUserFromSsoCenter(String token) {
        //普通方法中获取bean
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("token", token);
            map.put("serviceId", serviceId);
            String data = doPostForm(tokenCheckUrl, map);
            JSONObject json = JSON.parseObject(data);
            if ("0".equals(json.getString("code"))) {
                String resultData = json.getString("data");
                return JSON.parseObject(resultData, CommonWebUser.class);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }
    /**
     * 这个方法是java默认实现，是为了避免httpclient引入jar包与业务系统的冲突
     * @param httpUrl  请求的url
     * @param param  form表单的参数（key,value形式）
     * @return
     */
    private String doPostForm(String httpUrl, Map param) {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
            // 经过远程url链接对象打开链接
            connection = (HttpURLConnection) url.openConnection();
            // 设置链接请求方式
            connection.setRequestMethod("POST");
            // 设置链接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取主机服务器返回数据超时时间：60000毫秒
            connection.setReadTimeout(60000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，须要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数无关紧要
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            //connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // 经过链接对象获取一个输出流
            os = connection.getOutputStream();
            // 经过输出流对象将参数写出去/传输出去,它是经过字节数组写出的(form表单形式的参数实质也是key,value值的拼接，相似于get请求参数的拼接)
            os.write(createLinkString(param).getBytes());
            // 经过链接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 对输入流对象进行包装:charset根据工做项目组的要求来设置
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 断开与远程地址url的链接
            connection.disconnect();
        }
        return result;
    }
    /**
     * 把数组全部元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 须要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    private  String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder prestr = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr.append(key).append("=").append(value);
            } else {
                prestr.append(key).append("=").append(value).append("&");
            }
        }
        return prestr.toString();
    }
}
