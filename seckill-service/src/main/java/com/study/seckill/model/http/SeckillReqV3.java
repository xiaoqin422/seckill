package com.study.seckill.model.http;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 包名: com.study.seckill.model.http
 * 类名: SeckillReqV3
 * 创建用户: 25789
 * 创建日期: 2022年10月29日 16:40
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Data
public class SeckillReqV3 implements Serializable {
    @NotNull(message = "产品id 不能为空")
    private Long productId;
    private Long userId;
    @NotEmpty(message = "图片验证码标识 不能为空")
    private String imageId;
    @NotEmpty(message = "图片验证码 不能为空")
    private String imageCode;
}