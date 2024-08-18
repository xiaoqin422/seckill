package com.study.rocket.basic;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

@Component
public class SpringProducer {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    //发送普通消息的示例
    public void sendMessage(String topic,String msg){
        rocketMQTemplate.convertAndSend(topic,msg);
    }

    public void sendMessageInTransaction(String topic,String msg) throws InterruptedException {
        String[] tags = new String[]{"TagA","TagB","TagC","TagD","TagE"};
        for (int i = 0; i < 10; i++) {
            Message<String> message = MessageBuilder.withPayload(msg).build();
            String destination = topic + ":" + tags[i % tags.length];
            SendResult sendResult = rocketMQTemplate.sendMessageInTransaction(destination, message, destination);
            System.out.printf("%s%n",sendResult);
            TimeUnit.MICROSECONDS.sleep(10);
        }
    }
}
