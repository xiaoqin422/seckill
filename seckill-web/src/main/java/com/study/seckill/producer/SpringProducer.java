package com.study.seckill.producer;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SpringProducer {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    //发送普通消息的示例
    public void sendMessage(String topic, String tags, String msg) {
//        Message message = MessageBuilder.withPayload(msg).setHeader("KEYS", IdUtil.simpleUUID()).build();
//        this.rocketMQTemplate.convertAndSend(topic + ":" + tags, message);
        this.rocketMQTemplate.syncSend(topic + ":" + tags, msg);
    }

    //发送事务消息的示例
    public void sendMessageInTransaction(String topic, String msg) throws InterruptedException {
        String[] tags = new String[]{"TagA", "TagB", "TagC", "TagD", "TagE"};
        for (int i = 0; i < 10; i++) {
            Message<String> message = MessageBuilder.withPayload(msg).build();
            String destination = topic + ":" + tags[i % tags.length];
            SendResult sendResult = rocketMQTemplate.sendMessageInTransaction(destination, message, destination);
            System.out.printf("%s%n", sendResult);
            Thread.sleep(10);
        }
    }
}
