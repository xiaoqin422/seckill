package com.study.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.seckill.model.http.SeckillReq;
import com.study.seckill.model.http.mq.SendMsgReq;
import com.study.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "GID_order", topic = "seckill", selectorExpression = "order")
public class OrderListener implements RocketMQListener<String> {
    @Autowired
    SeckillService seckillService;
    @Override
    public void onMessage(String message) {
        log.info("===[成功消费《订单后续处理》消息：message={}]===", JSON.toJSONString(message));
        SendMsgReq.OrderReq req = JSON.parseObject(message, new TypeReference<SendMsgReq.OrderReq>() {
        });
        log.info("===productId={}===", req.getProductId());
        try {
            seckillService.createOrder(new SeckillReq().setProductId(req.getProductId()).setUserId(req.getUserId()));
        } catch (Exception e) {
            log.error("===[消费 “订单异步处理” 发生异常!]===", e);
        }
    }
}
