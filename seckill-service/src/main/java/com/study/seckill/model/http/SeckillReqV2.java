package com.study.seckill.model.http;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 包名: com.study.seckill.model.http
 * 类名: SeckillReqV2
 * 创建用户: 25789
 * 创建日期: 2022年10月29日 16:19
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Data
public class SeckillReqV2 implements Serializable {
    @NotNull(message = "产品id 不能为空")
    private Long productId;
    private Long userId;
    @NotBlank(message = "验证码 不能为空")
    private String verifyCode;
}