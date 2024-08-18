package com.study.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(value = "com.study.seckill.dao")
@ServletComponentScan(basePackages = {"com.study.seckill.security"})
public class SeckillWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillWebApplication.class, args);
    }
}
