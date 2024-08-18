package com.study.seckill.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.common.exception.ErrorMessage;
import com.study.seckill.common.exception.SecKillException;
import com.study.seckill.dao.SeckillOrderDao;
import com.study.seckill.dao.SeckillProductsDao;
import com.study.seckill.dao.SeckillUserDao;
import com.study.seckill.model.SeckillOrder;
import com.study.seckill.model.SeckillProducts;
import com.study.seckill.model.http.SeckillReq;
import com.study.seckill.model.http.SeckillReqV2;
import com.study.seckill.model.http.SeckillReqV3;
import com.study.seckill.service.SeckillService;
import com.study.seckill.common.base.Constant;
import com.study.seckill.common.util.cache.DecrCacheStockUtil;
import com.study.seckill.common.util.cache.DistrubuteLimit;
import com.study.seckill.common.util.cache.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 包名: com.study.seckill.service.impl
 * 类名: SeckillServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年10月12日 16:16
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private ProductServiceImpl productService;
    @Autowired
    private SeckillProductsDao productsDao;
    @Autowired
    private SeckillOrderDao orderDao;
    @Autowired
    private DecrCacheStockUtil decrCacheStockUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Redisson redisson;
    @Autowired
    private DistrubuteLimit distrubuteLimit;
    // Guava令牌桶，每秒生成5个令牌。
    private RateLimiter rateLimiter = RateLimiter.create(5);
    @Override
    public BaseResponse sOrder(SeckillReq req) {
        log.info("===[开始调用原始下单接口（多线程多机部署不安全）~]===");
        //参数校验
        log.info("===[校验用户信息及商品信息]===");
        BaseResponse paramValidRes = validateParam(req.getProductId(), req.getUserId());
        if (paramValidRes.getCode() != 0){
            return paramValidRes;
        }
        log.info("===[校验参数是否合法][通过]===");
        Long productId = req.getProductId();
        Long userId = req.getUserId();
        SeckillProducts product = productsDao.selectByPrimaryKey(productId);
        Date date = new Date();
        // 扣减库存
        log.info("===[开始扣减库存]===");
        product.setSaled(product.getSaled() + 1);
        productsDao.updateByPrimaryKeySelective(product);
        log.info("===[扣减库存][成功]===");
        // 创建订单
        log.info("===[开始创建订单]===");
        SeckillOrder order = new SeckillOrder();
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setUserId(userId);
        order.setCreateTime(date);
        orderDao.insert(order);
        log.info("===[创建订单][成功]===");
        return BaseResponse.OK(Boolean.TRUE);
    }

    /**
     * 悲观锁实现方式
     *
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor  = Exception.class)
    public BaseResponse pOrder(SeckillReq req) {
        log.info("===[开始调用[MySQL写锁解决并发问题]下单接口~]===");
        //参数校验
        log.info("===[校验用户信息及商品信息]===");
        //mysql对抢购商品添加记录锁
        BaseResponse paramValidRes = validateParamPessimistic(req.getProductId(), req.getUserId());
        if (paramValidRes.getCode() != 0){
            return paramValidRes;
        }
        log.info("===[校验参数是否合法][通过]===");
        Long productId = req.getProductId();
        Long userId = req.getUserId();
        SeckillProducts product = productsDao.selectByPrimaryKey(productId);
        Date date = new Date();
        // 扣减库存
        log.info("===[开始扣减库存]===");
        product.setSaled(product.getSaled() + 1);
        productsDao.updateByPrimaryKeySelective(product);
        log.info("===[扣减库存][成功]===");
        // 创建订单
        log.info("===[开始创建订单]===");
        SeckillOrder order = new SeckillOrder();
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setUserId(userId);
        order.setCreateTime(date);
        orderDao.insert(order);
        log.info("===[创建订单][成功]===");
        return BaseResponse.OK(Boolean.TRUE);
    }

    /**
     * 避免超卖  数据库乐观锁
     * 一人一单  redis乐观锁+数据库唯一索引
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse oOrder(SeckillReq req) throws Exception {
        log.info("===[开始调用下单接口~（乐观锁）]===");
        //参数校验
        log.info("===[校验参数是否合法]===");
        BaseResponse paramValidRes = validateParam(req.getProductId(), req.getUserId());
        if (paramValidRes.getCode() != 0) {
            return paramValidRes;
        }
        log.info("===[校验参数是否合法][通过]===");
        //下单（乐观锁）
        return createOptimisticOrder(req.getProductId(), req.getUserId());
    }

    /**
     * 避免超卖  redis单线程乐观锁库存扣减
     * 一人一单  数据库唯一索引+redis乐观锁
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse cOrder(SeckillReq req) throws Exception {
        log.info("===[开始调用下单接口~（避免超卖——Redis）]===");
        long res = 0;
        try {
            //校验用户信息、商品信息、库存信息
            log.info("===[校验用户信息、商品信息、库存信息]===");
            BaseResponse paramValidRes = validateParam(req.getProductId(), req.getUserId());
            if (paramValidRes.getCode() != 0) {
                return paramValidRes;
            }
            log.info("===[校验][通过]===");
            Long productId = req.getProductId();
            Long userId = req.getUserId();
            //redis + lua
            res = decrCacheStockUtil.decrStock(req.getProductId());
            if (res == 2) {
                // 扣减完的库存只要大于等于0，就说明扣减成功
                // 开始数据库扣减库存逻辑
                productsDao.decrStock(productId);
                // 创建订单
                SeckillProducts product = productsDao.selectByPrimaryKey(productId);
                Date date = new Date();
                SeckillOrder order = new SeckillOrder();
                order.setProductId(productId);
                order.setProductName(product.getName());
                order.setUserId(userId);
                order.setCreateTime(date);
                orderDao.insert(order);
                //记录已购买用户
                try {
                    addOrderedUserCache(productId, userId);
                } catch (Exception e) {
                    log.error("===[记录已购用户缓存时发生异常！]===", e);
                }
                return BaseResponse.OK;
            } else {
                log.error("===[缓存扣减库存不足！]===");
                return BaseResponse.error(ErrorMessage.STOCK_NOT_ENOUGH);
            }
        } catch (Exception e) {
            log.error("===[异常！]===", e);
            if (res == 2) {
                decrCacheStockUtil.addStock(req.getProductId());
            }
            throw new Exception("异常！");
        }
    }

    /**
     * 防止超卖  + 一人一单   redisson分布式锁一站式解决方案
     * @param req 前端请求
     * @return 服务器响应
     */
    @Override
    public BaseResponse redissonOrder(SeckillReq req) {
        log.info("===[开始调用下单接口（redission）~]===");
        String lockKey = String.format(Constant.redisKey.SECKILL_DISTRIBUTED_LOCK, req.getProductId());
        RLock lock = redisson.getLock(lockKey);
        try {
            //将锁过期时间设置为30s，定时任务每隔10秒执行续锁操作
            lock.lock();
            //** 另一种写法 **
            //先拿锁，在设置超时时间，看门狗就不会自动续期，锁到达过期时间后，就释放了
            //lock.lock(30, TimeUnit.SECONDS);
            //参数校验
            log.info("===[校验用户信息及商品信息]===");
            BaseResponse paramValidRes = validateParam(req.getProductId(), req.getUserId());
            if (paramValidRes.getCode() != 0) {
                return paramValidRes;
            }
            log.info("===[校验参数是否合法][通过]===");
            Long productId = req.getProductId();
            Long userId = req.getUserId();
            SeckillProducts product = productsDao.selectByPrimaryKey(productId);
            Date date = new Date();
            // 扣减库存
            log.info("===[开始扣减库存]===");
            product.setSaled(product.getSaled() + 1);
            productsDao.updateByPrimaryKeySelective(product);
            log.info("===[扣减库存][成功]===");
            // 创建订单
            log.info("===[开始创建订单]===");
            SeckillOrder order = new SeckillOrder();
            order.setProductId(productId);
            order.setProductName(product.getName());
            order.setUserId(userId);
            order.setCreateTime(date);
            orderDao.insert(order);
            log.info("===[创建订单][成功]===");
            //记录已购买用户
            try {
                addOrderedUserCache(productId, userId);
            } catch (Exception e) {
                log.error("===[记录已购用户缓存时发生异常！]===", e);
            }
            return BaseResponse.OK;
        } catch (Exception e) {
            log.error("===[异常！]===", e);
            return BaseResponse.error(ErrorMessage.SYS_ERROR);
        }finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse orderV1(SeckillReq req) throws Exception {
        log.info("===[开始调用下单接口（限流）]===");
        log.info("===[开始经过分布式限流程序]===");
        //分布式限流 单ip指定时间内访问次数
        try {
            if (!distrubuteLimit.exec()) {
                log.info("你被分布式锁限流了！直接返回失败！");
                return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
            }
        } catch (IOException e) {
            log.error("===[分布式限流程序发生异常！]===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
        log.info("===[分布式限流程序][通过]===");
        /**
         *  增加应用限流
         *  阻塞式 & 非阻塞式
         */
        log.info("===[开始经过限流程序]===");
        //  阻塞式获取令牌
        //  log.info("===[令牌桶限流:等待时间{}]===", rateLimiter.acquire());
        //  非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.error("你被限流了！直接返回失败！");
            return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
        }
        log.info("===[限流程序][通过]===");
        //校验用户信息、商品信息、库存信息
        log.info("===[校验用户信息、商品信息、库存信息]===");
        BaseResponse paramValidRes = validateParam(req.getProductId(), req.getUserId());
        if (paramValidRes.getCode() != 0) {
            return paramValidRes;
        }
        log.info("===[校验][通过]===");
        //下单（乐观锁）
        return createOptimisticOrder(req.getProductId(), req.getUserId());
    }

    @Override
    public BaseResponse orderV2(SeckillReqV2 req) throws Exception {
        log.info("===[开始调用下单接口（限流）]===");
        log.info("===[开始经过分布式限流程序]===");
        //分布式限流 单ip指定时间内访问次数
        try {
            if (!distrubuteLimit.exec()) {
                log.info("你被分布式锁限流了！直接返回失败！");
                return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
            }
        } catch (IOException e) {
            log.error("===[分布式限流程序发生异常！]===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
        log.info("===[分布式限流程序][通过]===");
        /**
         *  增加应用限流
         *  阻塞式 & 非阻塞式
         */
        log.info("===[开始经过限流程序]===");
        //  阻塞式获取令牌
        //  log.info("===[令牌桶限流:等待时间{}]===", rateLimiter.acquire());
        //  非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.error("你被限流了！直接返回失败！");
            return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
        }
        log.info("===[限流程序][通过]===");
        //校验用户信息、商品信息、库存信息
        log.info("===[校验用户信息、商品信息、库存信息、验证码]===");
        BaseResponse paramValidRes = validateParamV2(req.getProductId(), req.getUserId(),req.getVerifyCode());
        if (paramValidRes.getCode() != 0) {
            return paramValidRes;
        }
        log.info("===[校验][通过]===");
        //下单（乐观锁）
        return createOptimisticOrder(req.getProductId(), req.getUserId());
    }

    @Override
    public BaseResponse orderV3(SeckillReqV3 req) throws Exception {
        log.info("===[开始调用下单接口（限流）]===");
        log.info("===[开始经过分布式限流程序]===");
        //分布式限流 单ip指定时间内访问次数
        try {
            if (!distrubuteLimit.exec()) {
                log.info("你被分布式锁限流了！直接返回失败！");
                return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
            }
        } catch (IOException e) {
            log.error("===[分布式限流程序发生异常！]===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
        log.info("===[分布式限流程序][通过]===");
        /**
         *  增加应用限流
         *  阻塞式 & 非阻塞式
         */
        log.info("===[开始经过限流程序]===");
        //  阻塞式获取令牌
        //  log.info("===[令牌桶限流:等待时间{}]===", rateLimiter.acquire());
        //  非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.error("你被限流了！直接返回失败！");
            return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
        }
        log.info("===[限流程序][通过]===");
        //校验用户信息、商品信息、库存信息
        log.info("===[校验用户信息、商品信息、库存信息、验证码]===");
        BaseResponse paramValidRes = validateParamV3(req.getProductId(), req.getUserId(),req.getImageId(),req.getImageCode());
        if (paramValidRes.getCode() != 0) {
            return paramValidRes;
        }
        log.info("===[校验][通过]===");
        //下单（乐观锁）
        return createOptimisticOrder(req.getProductId(), req.getUserId());
    }

    @Override
    public BaseResponse orderV4(SeckillReqV2 req) throws Exception {
        log.info("===[开始调用下单接口（限流）]===");
        log.info("===[开始经过分布式限流程序]===");
        //分布式限流 单ip指定时间内访问次数  最好再nginx做ip限流（传输层）
        try {
            if (!distrubuteLimit.exec()) {
                log.info("你被分布式锁限流了！直接返回失败！");
                return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
            }
        } catch (IOException e) {
            log.error("===[分布式限流程序发生异常！]===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
        log.info("===[分布式限流程序][通过]===");
        /**
         *  增加应用限流
         *  阻塞式 & 非阻塞式
         */
        log.info("===[开始经过限流程序]===");
        //  阻塞式获取令牌
        //  log.info("===[令牌桶限流:等待时间{}]===", rateLimiter.acquire());
        //  非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.error("你被限流了！直接返回失败！");
            return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
        }
        log.info("===[限流程序][通过]===");
        //校验用户信息、商品信息、库存信息
        log.info("===[校验用户信息、商品信息、库存信息、验证码]===");
        BaseResponse paramValidRes = validateParamV4(req.getProductId(), req.getUserId(),req.getVerifyCode());
        if (paramValidRes.getCode() != 0) {
            return paramValidRes;
        }
        log.info("===[校验][通过]===");
        //下单（乐观锁）
        return createOptimisticOrder(req.getProductId(), req.getUserId());
    }

    @Override
    public BaseResponse orderV5(SeckillReqV2 req) throws Exception {
        log.info("===[开始调用下单接口（限流）]===");
        log.info("===[开始经过分布式限流程序]===");
        //分布式限流 单ip指定时间内访问次数  最好再nginx做ip限流（传输层）
        try {
            if (!distrubuteLimit.exec()) {
                log.info("你被分布式锁限流了！直接返回失败！");
                return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
            }
        } catch (IOException e) {
            log.error("===[分布式限流程序发生异常！]===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
        log.info("===[分布式限流程序][通过]===");
        /**
         *  增加应用限流
         *  阻塞式 & 非阻塞式
         */
        log.info("===[开始经过限流程序]===");
        //  阻塞式获取令牌
        //  log.info("===[令牌桶限流:等待时间{}]===", rateLimiter.acquire());
        //  非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.error("你被限流了！直接返回失败！");
            return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
        }
        log.info("===[限流程序][通过]===");
        //校验用户信息、商品信息、库存信息
        log.info("===[校验用户信息、商品信息、库存信息、验证码]===");
        BaseResponse paramValidRes = validateParamV4(req.getProductId(), req.getUserId(),req.getVerifyCode());
        if (paramValidRes.getCode() != 0) {
            return paramValidRes;
        }
        log.info("===[校验][通过]===");
        //下单（乐观锁）
        return createOptimisticOrderV5(req.getProductId(), req.getUserId());
    }

    /**
     * 问题： 请求下单时，  库存扣减成功。异步发送下单，异步订单未完成前，用一个用户同一件商品也扣减库存成功->
     *                      重复下单失败，幂等性，缓存中的库存跟实际库存无法保证最终一致性
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    public BaseResponse orderV8(SeckillReqV2 req) throws Exception {
        log.info("===[开始调用下单接口（限流）]===");
        log.info("===[开始经过分布式限流程序]===");
        //分布式限流 单ip指定时间内访问次数  最好再nginx做ip限流（传输层）
        try {
            if (!distrubuteLimit.exec()) {
                log.info("你被分布式锁限流了！直接返回失败！");
                return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
            }
        } catch (IOException e) {
            log.error("===[分布式限流程序发生异常！]===", e);
            return BaseResponse.error(ErrorMessage.SECKILL_FAILED);
        }
        log.info("===[分布式限流程序][通过]===");
        /**
         *  增加应用限流
         *  阻塞式 & 非阻塞式
         */
        log.info("===[开始经过限流程序]===");
        //  阻塞式获取令牌
        //  log.info("===[令牌桶限流:等待时间{}]===", rateLimiter.acquire());
        //  非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.error("你被限流了！直接返回失败！");
            return BaseResponse.error(ErrorMessage.SECKILL_RATE_LIMIT_ERROR);
        }
        log.info("===[限流程序][通过]===");
        long res = 0;
        try {
            //校验用户信息、商品信息、库存信息
            log.info("===[校验用户信息、商品信息、库存信息、验证码]===");
            BaseResponse paramValidRes = validateParamV4(req.getProductId(), req.getUserId(),req.getVerifyCode());
            if (paramValidRes.getCode() != 0) {
                return paramValidRes;
            }
            log.info("===[校验][通过]===");
            res = decrCacheStockUtil.decrStock(req.getProductId());
            if (res == 2){
                // 扣减完的库存只要大于等于0，就说明扣减成功
                // 开始数据库扣减库存逻辑
                return BaseResponse.OK;
            }else {
                log.error("===[缓存扣减库存不足！]===");
                return BaseResponse.error(ErrorMessage.STOCK_NOT_ENOUGH);
            }
        }catch (Exception e){
            log.error("===[异常！]===", e);
            if (res == 2) {
                decrCacheStockUtil.addStock(req.getProductId());
            }
            throw new SecKillException("异常！");
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse createOrder(SeckillReq req) throws Exception {
        Long productId = req.getProductId();
        Long userId = req.getUserId();
        // 创建订单
        SeckillProducts products = productsDao.selectByPrimaryKey(productId);
        Date date = new Date();
        SeckillOrder order = new SeckillOrder();
        order.setProductId(productId);
        order.setProductName(products.getName());
        order.setUserId(userId);
        order.setCreateTime(date);
        // 利用唯一索引，保证幂等性，一旦消费重复的消息，则此处会抛出异常
        orderDao.insert(order);
        log.info("===[下单逻辑][创建订单成功]===");
        //扣减库存
        productsDao.decrStock(productId);
        // 将已下单用户放入缓存中
        log.info("===[开始将已下单用户放入缓存！]===");
        addOrderedUserCache(productId, userId);
        return BaseResponse.OK;
    }
    private BaseResponse validateParamV4(Long productId, Long userId, String verifyCode) {
        //单用户访问频次限制
        String visitKey = String.format(Constant.redisKey.SECKILL_USER_VISIT, productId, userId);
        long visitCount = redisUtil.incr(visitKey, 1L);
        redisUtil.expire(visitKey, 5);
        if (visitCount > Constant.VISIT_LIMIT) {
            return BaseResponse.error(ErrorMessage.SECKILL_USER_VISIT_LIMIT_ERROR);
        }
        log.info("===[单用户频次限制合法]===");
        return validateParamV2(productId, userId, verifyCode);
    }

    private BaseResponse validateParamV3(Long productId, Long userId, String imageId, String imageCode){
        //校验图形验证码
        String key = String.format(Constant.redisKey.SECKILL_IMAGE_CODE,imageId);
        if (!redisUtil.hasKey(key) || !imageCode.equals(String.valueOf(redisUtil.get(key)))){
            redisUtil.del(key);
            log.error("===[图形验证码错误！]===");
            return BaseResponse.error(ErrorMessage.SECKILL_VALIDATE_ERROR);
        }
        return validateParam(productId,userId);
    }
    private BaseResponse validateParamV2(Long productId, Long userId, String verifyCode) {
        //校验验证码
        String key = String.format(Constant.redisKey.SECKILL_VALIDATE_CODE,productId,userId);
        if (!redisUtil.hasKey(key) || !verifyCode.equals(String.valueOf(redisUtil.get(key)))){
            redisUtil.del(key);
            log.error("===[下单验证码错误！]===");
            return BaseResponse.error(ErrorMessage.SECKILL_VALIDATE_ERROR);
        }
        return validateParam(productId, userId);
    }

    @Override
    public BaseResponse getVerifyHash(SeckillReq req) {
        log.info("===[开始调用获取秒杀接口验证码接口]===");
        //校验用户信息、商品信息、库存信息
        log.info("===[校验用户信息、商品信息、库存信息]===");
        BaseResponse paramValidRes = validateParam(req.getProductId(), req.getProductId());
        if (paramValidRes.getCode() != 0){
            return paramValidRes;
        }
        log.info("===[校验通过]===");

        //生成Hash
        String verify = Constant.VALIDATE_CODE_SALT + req.getProductId() + req.getUserId();
        String verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes(StandardCharsets.UTF_8));
        //将hash和用户商品信息存入redis
        String key = String.format(Constant.redisKey.SECKILL_VALIDATE_CODE, req.getProductId(), req.getUserId());
        redisUtil.set(key,verifyHash,60);
        return BaseResponse.OK(verifyHash);
    }

    @Override
    public Integer getStockByDB(Long productId) {
        SeckillProducts products = productsDao.selectByPrimaryKey(productId);
        return products.getCount() - products.getSaled();
    }

    @Override
    public Integer getStockByCache(Long productId) {
        String key = String.format(Constant.redisKey.SECKILL_SALED_COUNT, productId);
        Object count = redisUtil.get(key);
        if (count == null){
            String lockKey = String.format(Constant.redisKey.SECKILL_DISTRIBUTED_LOCK, productId);
            RLock lock = redisson.getLock(lockKey);
            try {
                lock.lock();
                if ((count = redisUtil.get(key)) == null){
                    count = getStockByDB(productId);
                    redisUtil.set(key,count);
                }
            }finally {
                lock.unlock();
            }
        }
        return Integer.valueOf(String.valueOf(count));
    }

    private BaseResponse createOptimisticOrder(Long productId, Long userId) throws Exception {
        log.info("===[下单逻辑Starting]===");
        Date date = new Date();
        SeckillProducts product = productsDao.selectByPrimaryKey(productId);
        SeckillOrder order = new SeckillOrder();
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setUserId(userId);
        order.setCreateTime(date);
        orderDao.insert(order);
        log.info("===[创建订单][成功]===");
        // 扣减库存
        log.info("===[开始扣减库存]===");
        int res = productsDao.updateStockByOptimistic(productId);
        if (res == 0) {
            log.error("===[秒杀失败，抛出异常，执行回滚逻辑！]===");
            throw new SecKillException(ErrorMessage.STOCK_NOT_ENOUGH.getMessage());
        }
        log.info("===[扣减库存][成功]===");
        try {
            addOrderedUserCache(productId, userId);
        } catch (Exception e) {
            log.error("===[记录已购用户缓存时发生异常！]===", e);
        }
        return BaseResponse.OK(Boolean.TRUE);
    }
    private BaseResponse createOptimisticOrderV5(Long productId, Long userId) throws Exception {
        //先删除缓存 再更新数据库
        this.delStockByCache(productId);

        log.info("===[下单逻辑Starting]===");
        Date date = new Date();
        SeckillProducts product = productsDao.selectByPrimaryKey(productId);
        SeckillOrder order = new SeckillOrder();
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setUserId(userId);
        order.setCreateTime(date);
        orderDao.insert(order);
        log.info("===[创建订单][成功]===");
        // 扣减库存
        log.info("===[开始扣减库存]===");
        int res = productsDao.updateStockByOptimistic(productId);
        if (res == 0) {
            log.error("===[秒杀失败，抛出异常，执行回滚逻辑！]===");
            throw new SecKillException(ErrorMessage.STOCK_NOT_ENOUGH.getMessage());
        }
        log.info("===[扣减库存][成功]===");

        //先更新数据库 再删除缓存
        //this.delStockByCache(productId);
        try {
            addOrderedUserCache(productId, userId);
        } catch (Exception e) {
            log.error("===[记录已购用户缓存时发生异常！]===", e);
        }
        return BaseResponse.OK(Boolean.TRUE);
    }

    @Override
    public void delStockByCache(Long productId) {
        String key = String.format(Constant.redisKey.SECKILL_SALED_COUNT, productId);
        redisUtil.del(key);
        log.info("===[删除商品id：[{}] 缓存]===", productId);
    }

    private BaseResponse validateParamPessimistic(Long productId, Long userId) {
        SeckillProducts product = productsDao.selectForUpdate(productId);
        if (product == null) {
            log.error("===[产品不存在！]===");
            return BaseResponse.error(ErrorMessage.SYS_ERROR);
        }
        if (product.getStartBuyTime().getTime() > System.currentTimeMillis()) {
            log.error("===[秒杀还未开始！]===");
            return BaseResponse.error(ErrorMessage.SECKILL_NOT_START);
        }
        if (product.getSaled() >= product.getCount()) {
            log.error("===[库存不足！]===");
            return BaseResponse.error(ErrorMessage.STOCK_NOT_ENOUGH);
        }
        return BaseResponse.OK;
    }

    private BaseResponse validateParam(Long productId, Long userId) {
        //缓存中拿
        SeckillProducts product = productService.selectByPrimaryKey(productId);
        if (product == null) {
            log.error("===[产品不存在！]===");
            return BaseResponse.error(ErrorMessage.SYS_ERROR);
        }
        if (product.getStartBuyTime().getTime() > System.currentTimeMillis()) {
            log.error("===[秒杀还未开始！]===");
            return BaseResponse.error(ErrorMessage.SECKILL_NOT_START);
        }
        if (product.getCount() >= getStockByCache(productId)) {
            log.error("===[库存不足！]===");
            return BaseResponse.error(ErrorMessage.STOCK_NOT_ENOUGH);
        }
        if (hasOrderedUserCache(productId, userId)) {
            log.error("===[用户重复下单！]===");
            return BaseResponse.error(ErrorMessage.REPEAT_ORDER_ERROR);
        }
        return BaseResponse.OK;
    }
    private void addOrderedUserCache(Long productId, Long userId) {
        String key = String.format(Constant.redisKey.SECKILL_ORDERED_USER, productId);
        redisUtil.sSet(key, userId);
        log.info("===[已将已购用户放入缓存！]===");
    }
    @Override
    public Boolean hasOrderedUserCache(Long productId, Long userId) {
        String key = String.format(Constant.redisKey.SECKILL_ORDERED_USER, productId);
        return redisUtil.sHasKey(key, userId);
    }
}