package com.study.rocket.controller;

import com.study.rocket.basic.SpringProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.soap.Addressing;

@RestController
@RequestMapping("MQTest")
public class SendMessageController {
    @Autowired
    SpringProducer springProducer;
    private static final String TOPIC = "TestTopic";

    @GetMapping("sendMessage")
    public void SendMessage(@RequestParam("message") String msg){
        springProducer.sendMessage(TOPIC,msg);
    }
    @GetMapping("sendTransactionMessage")
    public void SendTransactionMessage(@RequestParam("message") String msg) throws InterruptedException {
        springProducer.sendMessageInTransaction(TOPIC,msg);
    }
}
