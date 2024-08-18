package com.study.seckill.controller;

import com.study.seckill.model.Demo;
import com.study.seckill.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/demo")
@Slf4j
public class DemoController {

    @Autowired
    private DemoService demoService;


    @RequestMapping(value = "/test")
    public List<Demo> test() {
        return demoService.list();
    }
}
