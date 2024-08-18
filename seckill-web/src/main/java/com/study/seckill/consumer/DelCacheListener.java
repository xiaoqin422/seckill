package com.study.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.seckill.model.http.mq.SendMsgReq;
import com.study.seckill.service.SeckillService;
import com.study.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消费监听：删除库存缓存
 *
 * @author: han
 * @since: 2020-07-23 11:47
 **/
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "GID_del_cache", topic = "seckill", selectorExpression = "delCache")
public class DelCacheListener implements RocketMQListener<String> {
    @Autowired
    SeckillService seckillService;

    @Override
    public void onMessage(String message) {
        log.info("===[成功消费《删除库存缓存》消息：message={}]===", JSON.toJSONString(message));
        SendMsgReq.DelCacheReq req = JSON.parseObject(JSON.parseObject(message).getJSONObject("payload").toJSONString(), new TypeReference<SendMsgReq.DelCacheReq>() {
        });
        log.info("===productId={}===", req.getProductId());
        seckillService.delStockByCache(req.getProductId());
    }
}
