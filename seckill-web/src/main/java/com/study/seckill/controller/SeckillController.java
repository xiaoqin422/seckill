package com.study.seckill.controller;

import com.study.seckill.common.base.BaseRequest;
import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.common.entity.CommonWebUser;
import com.study.seckill.common.exception.ErrorMessage;
import com.study.seckill.model.http.SeckillReq;
import com.study.seckill.model.http.SeckillReqV2;
import com.study.seckill.model.http.SeckillReqV3;
import com.study.seckill.model.http.mq.SendMsgReq;
import com.study.seckill.security.WebUserUtil;
import com.study.seckill.service.SeckillService;
import com.study.seckill.util.MqSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 包名: com.study.seckill.controller
 * 类名: SeckillController
 * 创建用户: 25789
 * 创建日期: 2022年10月12日 16:12
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@RestController
@RequestMapping("/seckill")
@Slf4j
public class SeckillController {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private MqSender mqService;
    private static final int DELAY_MILLSECONDS = 1000;
    // 延时双删线程池
    private static ExecutorService cachedThreadPool = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    /**
     * 秒杀下单（原始下单逻辑）
     */
    @RequestMapping(value = "/simple/order")
    public synchronized BaseResponse sOrder(@Valid @RequestBody BaseRequest<SeckillReq> request){
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)){
            return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
        }
        SeckillReq req = request.getData();
        req.setUserId(user.getId());
        return seckillService.sOrder(req);
    }
    /**
     * 秒杀下单（避免超卖问题——数据库悲观锁）
     */
    @RequestMapping(value = "/pessimistic/order")
    public BaseResponse pOrder(@Valid @RequestBody BaseRequest<SeckillReq> request) {
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)) {
            return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
        }
        SeckillReq req = request.getData();
        req.setUserId(user.getId());
        return seckillService.pOrder(req);
    }
    /**
     * 秒杀下单（避免超卖问题——数据库乐观锁）
     */
    @RequestMapping(value = "/optimistic/order")
    public BaseResponse oOrder(@Valid @RequestBody BaseRequest<SeckillReq> request) throws Exception {
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)) {
            return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
        }
        SeckillReq req = request.getData();
        req.setUserId(user.getId());
        return seckillService.oOrder(req);
    }
    /**
     * 秒杀下单（避免超卖问题——redis缓存库存，保证原子扣减库存）
     */
    @RequestMapping(value = "/cache/order")
    public BaseResponse cOrder(@Valid @RequestBody BaseRequest<SeckillReq> request) {
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            SeckillReq req = request.getData();
            req.setUserId(user.getId());
            return seckillService.cOrder(req);
        } catch (Exception e) {
            log.error("===秒杀异常！===", e);
        }
        return BaseResponse.error(ErrorMessage.SYS_ERROR);
    }
    /**
     * 秒杀下单（避免超卖问题——redission）
     */
    @RequestMapping(value = "/redission/order")
    public BaseResponse rOrder(@Valid @RequestBody BaseRequest<SeckillReq> request) {
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)) {
            return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
        }
        SeckillReq req = request.getData();
        req.setUserId(user.getId());
        return seckillService.redissonOrder(req);
    }
    /**
     * 秒杀下单优化（应用限流）
     */
    @RequestMapping(value = "/v1/order")
    public BaseResponse orderV1(@Valid @RequestBody BaseRequest<SeckillReq> request) {
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            SeckillReq req = request.getData();
            req.setUserId(user.getId());
            return seckillService.orderV1(req);
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
        }
        return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
    }
    /**
     * 秒杀url隐藏——前置验证接口
     */
    @RequestMapping(value = "/verifyHash")
    public BaseResponse getVerifyHash(@Valid @RequestBody BaseRequest<SeckillReq> request) {
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)) {
            return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
        }
        SeckillReq req = request.getData();
        req.setUserId(user.getId());
        return seckillService.getVerifyHash(req);
    }
    /**
     * 秒杀下单优化（秒杀url隐藏）
     */
    @RequestMapping(value = "/v2/order")
    public BaseResponse orderV2(@Valid @RequestBody BaseRequest<SeckillReqV2> request) {
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            SeckillReqV2 req = request.getData();
            req.setUserId(user.getId());
            return seckillService.orderV2(req);
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
        }
        return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
    }
    /**
     * 秒杀下单优化（秒杀url隐藏,图形验证码）
     */
    @RequestMapping(value = "/v3/order")
    public BaseResponse orderV3(@Valid @RequestBody BaseRequest<SeckillReqV3> request) {
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            SeckillReqV3 req = request.getData();
            req.setUserId(user.getId());
            return seckillService.orderV3(req);
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
        }
        return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
    }
    @RequestMapping(value = "/v4/order")
    public BaseResponse orderV4(@Valid @RequestBody BaseRequest<SeckillReqV2> request) {
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            SeckillReqV2 req = request.getData();
            req.setUserId(user.getId());
            return seckillService.orderV4(req);
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
        }
        return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
    }
    @RequestMapping(value = "/db/stock")
    public BaseResponse<Integer> getStockByDB(@Valid @RequestBody BaseRequest<SeckillReq> request) {
        try {
            SeckillReq req = request.getData();
            return BaseResponse.ok(seckillService.getStockByDB(req.getProductId()));
        } catch (Exception e) {
            return BaseResponse.error(ErrorMessage.SYS_ERROR);
        }
    }
    @RequestMapping(value = "/cache/stock")
    public BaseResponse<Integer> getStockByCache(@RequestBody BaseRequest<SeckillReq> request) {
        try {
            SeckillReq req = request.getData();
            return BaseResponse.ok(seckillService.getStockByCache(req.getProductId()));
        } catch (Exception e) {
            return BaseResponse.error(ErrorMessage.SYS_ERROR);
        }
    }

    /**
     * 秒杀下单优化（缓存数据库一致性——先删除缓存，再更新数据库）
     */
    @RequestMapping(value = "/v5/order")
    public BaseResponse orderV5(@Valid @RequestBody BaseRequest<SeckillReqV2> request) {
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            SeckillReqV2 req = request.getData();
            req.setUserId(user.getId());
            return seckillService.orderV5(req);
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
        }
        return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
    }
    /**
     * 延时双删
     */
    @RequestMapping(value = "/v6/order")
    public BaseResponse orderV6(@Valid @RequestBody BaseRequest<SeckillReqV2> request) {
        SeckillReqV2 req = request.getData();
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            req.setUserId(user.getId());
            return seckillService.orderV5(req);
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        } finally {
            // 延时指定时间后再次删除缓存
            cachedThreadPool.execute(new delCacheByThread(req.getProductId()));
        }
    }
        /**
         * 缓存再删除线程
         */
        private class delCacheByThread implements Runnable {
            private Long sid;
            public delCacheByThread(Long sid) {
                this.sid = sid;
            }
            public void run() {
                try {
                    log.info("异步执行缓存再删除，商品id：[{}]， 首先休眠：[{}] 毫秒", sid, DELAY_MILLSECONDS);
                    Thread.sleep(DELAY_MILLSECONDS);
                    seckillService.delStockByCache(sid);
                    log.info("再次删除商品id：[{}] 缓存", sid);
                } catch (Exception e) {
                    log.error("delCacheByThread执行出错", e);
                }
            }
        }
    /**
     * 删除缓存重试机制
     */
    @RequestMapping(value = "/v7/order")
    public BaseResponse orderV7(@Valid @RequestBody BaseRequest<SeckillReqV2> request) {
        SeckillReqV2 req = request.getData();
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            req.setUserId(user.getId());
            return seckillService.orderV5(req);
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        } finally {
            // 消息队列处理 “缓存删除重试” 逻辑
            mqService.sendDelCacheMsg(new SendMsgReq.DelCacheReq().setProductId(req.getProductId()));
        }
    }
    /**
     * 下单方法 —— 订单异步处理优化
     */
    @RequestMapping(value = "/v8/order")
    public BaseResponse orderV8(@Valid @RequestBody BaseRequest<SeckillReqV2> request) {
        SeckillReqV2 req = request.getData();
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            req.setUserId(user.getId());
            BaseResponse res = seckillService.orderV8(req);
            if (res.getCode() == 0) {
                mqService.sendOrderMsg(new SendMsgReq.OrderReq().setProductId(req.getProductId()).setUserId(user.getId()));
            }
            return res;
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
    }
    /**
     * nginx+lua通过后，调用预下单方法，交给MQ，待订单系统消费消息，完成订单创建
     */
    @RequestMapping(value = "/pre/order")
    public BaseResponse preOrder(@Valid @RequestBody BaseRequest<SeckillReq> request) {
        SeckillReq req = request.getData();
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)) {
                return BaseResponse.error(ErrorMessage.LOGIN_ERROR);
            }
            req.setUserId(user.getId());
            mqService.sendOrderMsg(new SendMsgReq.OrderReq().setProductId(req.getProductId()).setUserId(user.getId()));
        } catch (Exception e) {
            log.error("===秒杀发生异常！===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
        return BaseResponse.OK;
    }
    /**
     * 检查缓存中用户是否已经生成订单
     *
     * @return
     */
    @RequestMapping(value = "/final/orderQuery")
    public BaseResponse<String> orderQuery(@Valid @RequestBody SeckillReq req) {
        // 检查缓存中该用户是否已经下单过
        Boolean checkRepeat = seckillService.hasOrderedUserCache(req.getProductId(), req.getUserId());
        if (checkRepeat == null) {
            log.error("===[验证缓存中用户是否已经生成订单时异常！]===");
            return BaseResponse.error(ErrorMessage.SYS_ERROR);
        } else if (checkRepeat) {
            log.error("===[用户{}已经生成订单！]===", req.getUserId());
            return BaseResponse.ok("恭喜您，已经抢购成功！");
        }
        return BaseResponse.ok("很抱歉，你的订单尚未生成，请继续等待结果！");
    }
}