package com.study.seckill.common.util.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.study.seckill.common.util.context.SpringContextHolder;
import com.study.seckill.common.util.http.HttpClientUtil;
import com.study.seckill.common.util.http.HttpResult;

import java.util.HashMap;
import java.util.Map;

public class CommonSmsSdk {
    public static final String SMS_URL = "https://www.bimowo.com/kstudy-admin/pub/mockSendSmsCode.do";
    public static final String APP_ID = "shukil123tsbcg";

    /**
     * 发送短信模板请求
     *
     * @param to         必选参数 短信接收端手机号码集合，用英文逗号分开，每批发送的手机号数量不得超过100个
     * @param templateId 必选参数 模板Id
     * @param datas      可选参数 内容数据，用于替换模板中{序号}
     * @return
     */
    public static Map<String, Object> sendTemplateSMS(String to,
                                                      String templateId, String[] datas) {
        //模板的格式例如为：【签名，例如笔墨屋】您的验证码为${1},在${2}分钟内有效！
        //短信模板分为两类，通知短信（例如验证码）和营销短信（这个就是广告推销相关的），这是运营商的要求
        //普通方法中获取bean
        HttpClientUtil httpClientUtil = SpringContextHolder.getBean("httpClientUtil");
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("appId", APP_ID);
            map.put("to", to);
            map.put("templatedId", templateId);
            if (datas != null) {
                StringBuilder sb = new StringBuilder("[");
                for (String s : datas) {
                    sb.append("\"" + s + "\"" + ",");
                }
                sb.replace(sb.length() - 1, sb.length(), "]");
                JSONArray jarray = JSONArray.parseArray(sb.toString());
                map.put("datas", jarray);
            }
            HttpResult result = httpClientUtil.doBodyPost(SMS_URL, map);
            Map<String, Object> parseObject = JSON.parseObject(result.getBody(), Map.class);
            return parseObject;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
}
