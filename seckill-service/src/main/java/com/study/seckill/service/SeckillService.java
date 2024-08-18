package com.study.seckill.service;

import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.model.http.SeckillReq;
import com.study.seckill.model.http.SeckillReqV2;
import com.study.seckill.model.http.SeckillReqV3;
import org.springframework.transaction.annotation.Transactional;

/**
 * 包名: com.study.seckill.service
 * 类名: SeckillService
 * 创建用户: 25789
 * 创建日期: 2022年10月12日 16:16
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
public interface SeckillService {
    BaseResponse sOrder(SeckillReq req);
    BaseResponse pOrder(SeckillReq req);

    BaseResponse oOrder(SeckillReq req) throws Exception;

    BaseResponse cOrder(SeckillReq req) throws Exception;

    BaseResponse redissonOrder(SeckillReq req);

    BaseResponse orderV1(SeckillReq req) throws Exception;
    BaseResponse orderV2(SeckillReqV2 req) throws Exception;
    BaseResponse orderV3(SeckillReqV3 req) throws Exception;

    BaseResponse orderV4(SeckillReqV2 req) throws Exception;

    @Transactional(rollbackFor = Exception.class)
    BaseResponse createOrder(SeckillReq req) throws Exception;

    BaseResponse getVerifyHash(SeckillReq req);

    Integer getStockByDB(Long productId);

    Integer getStockByCache(Long productId);

    BaseResponse orderV5(SeckillReqV2 req) throws Exception;

    void delStockByCache(Long productId);

    BaseResponse orderV8(SeckillReqV2 req) throws Exception;

    Boolean hasOrderedUserCache(Long productId, Long userId);
}