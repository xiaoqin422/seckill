package com.study.seckill.model.http;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 包名: com.study.seckill.model.http
 * 类名: SeckillReq
 * 创建用户: 25789
 * 创建日期: 2022年10月12日 16:11
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Data
@Accessors(chain = true)
public class SeckillReq implements Serializable {
    @NotNull(message = "产品id 不能为空")
    private Long productId;

    private Long userId;
}