package com.study.seckill.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 包名: com.study.seckill.security
 * 类名: Token
 * 创建用户: 25789
 * 创建日期: 2022年10月10日 16:03
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Token implements Serializable {
    private static final long serialVersionUID = 6314027741784310221L;
    private String token;
    private Long loginTime;
    private Long userId;
}