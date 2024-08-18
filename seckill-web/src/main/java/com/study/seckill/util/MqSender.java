package com.study.seckill.util;

import com.alibaba.fastjson.JSON;
import com.study.seckill.model.http.mq.SendMsgReq;
import com.study.seckill.producer.SpringProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqSender {

    @Autowired
    private SpringProducer springProducer;

    @Value("${rocketmq.topic}")
    private String TOPIC;
    @Value("${rocketmq.consumer.cache.tags}")
    private String TAG_CACHE;
    @Value("${rocketmq.consumer.order.tags}")
    private String TAG_ORDER;
    public void sendDelCacheMsg(SendMsgReq.DelCacheReq req) {
        springProducer.sendMessage(TOPIC, TAG_CACHE, JSON.toJSONString(req));
    }

    public void sendOrderMsg(SendMsgReq.OrderReq req) {
        springProducer.sendMessage(TOPIC, TAG_ORDER, JSON.toJSONString(req));
    }
}
