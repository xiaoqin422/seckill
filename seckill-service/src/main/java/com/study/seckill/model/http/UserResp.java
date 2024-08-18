package com.study.seckill.model.http;

import lombok.Data;

import java.io.Serializable;

/**
 * 包名: com.study.seckill.model.http
 * 类名: UserResp
 * 创建用户: 25789
 * 创建日期: 2022年10月01日 18:15
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Data
public class UserResp implements Serializable {
    @Data
    public static class BaseUserResp implements Serializable{
        private String token;
    }
}