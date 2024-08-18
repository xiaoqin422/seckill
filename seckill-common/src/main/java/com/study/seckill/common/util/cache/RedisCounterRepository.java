package com.study.seckill.common.util.cache;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisCounterRepository {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //获取用户的foreginId
    public String getUserForeignId() {
        Date date = new Date();
        String nowDateStr = DateUtil.format(date, "yyyyMMddHHmmSS");
        Random r = new Random();
        String changeNumPrefix = r.nextInt(10) + "";
        Long value = incrementNum(nowDateStr);
        //不足4位补0，redis从1开始生成的，每天再次请0
        return changeNumPrefix + nowDateStr + StringUtils.leftPad(String.valueOf(value), 4, '0');
    }

    //从redis中获取自增数据(redis保证自增是原子操作),每天从1开始
    private long incrementNum(String key) {
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (null == factory) {
            log.error("Unable to connect to redis.");
            throw new RuntimeException("redis connect error!");
        }
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, factory);
        long increment = redisAtomicLong.incrementAndGet();
        if (1 == increment) {
            // 如果数据是初次设置,需要设置超时时间
            redisAtomicLong.expire(1, TimeUnit.DAYS);
        }
        return increment;
    }

}
