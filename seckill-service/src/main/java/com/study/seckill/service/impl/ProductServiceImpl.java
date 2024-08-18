package com.study.seckill.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.study.seckill.common.base.Constant;
import com.study.seckill.common.exception.SecKillException;
import com.study.seckill.common.util.page.bean.CommonQueryBean;
import com.study.seckill.dao.SeckillProductsDao;
import com.study.seckill.model.SeckillProducts;
import com.study.seckill.service.ProductService;
import com.study.seckill.common.util.cache.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 包名: com.study.seckill.service.impl
 * 类名: ProductServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年10月09日 20:57
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private SeckillProductsDao seckillProductsDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Redisson redisson;
    @Override
    public SeckillProducts selectByPrimaryKey(Long id) {
        String key = String.format(Constant.redisKey.SECKILL_PRODUCT_INFO,id);
        Object object = redisUtil.get(key);
        SeckillProducts products = null;
        //缓存没有查询到，缓存击穿。
        if (object == null){
            String lockKey = String.format(Constant.redisKey.SECKILL_DISTRIBUTED_LOCK, id);
            RLock lock = redisson.getLock(lockKey);
            try {
                lock.lock();
                //双端检索
                if ((object = redisUtil.get(key)) == null){
                    products = seckillProductsDao.selectByPrimaryKey(id);
                    redisUtil.set(key,products);
                }
            }finally {
                lock.unlock();
            }
        }else {
            products = (SeckillProducts) object;
        }
        return products;
    }

    @Override
    public int insert(SeckillProducts record) {
        return seckillProductsDao.insert(record);
    }

    @Override
    public int updateByPrimaryKeySelective(SeckillProducts record) {
        int res = seckillProductsDao.updateByPrimaryKeySelective(record);
        String key = String.format(Constant.redisKey.SECKILL_PRODUCT_INFO,record.getId());
        //先更新再删除
        if (redisUtil.get(key) != null){
            redisUtil.del(key);
        }
        return res;
    }

    @Override
    public List<SeckillProducts> list4Page(SeckillProducts record, CommonQueryBean query) {
        return seckillProductsDao.list4Page(record,query);
    }

    @Override
    public long count(SeckillProducts record) {
        return seckillProductsDao.count(record);
    }

    @Override
    public List<SeckillProducts> list(SeckillProducts record) {
        return seckillProductsDao.list(record);
    }

    @Override
    public Long uniqueInsert(SeckillProducts record) {
        try {
            record.setCreateTime(new Date());
            record.setIsDeleted(0);
            record.setStatus(SeckillProducts.STATUS_IS_OFFLINE);

            SeckillProducts existItem = findByProductPeriodKey(record.getProductPeriodKey());
            if (existItem != null) return existItem.getId();
            else return Long.valueOf(seckillProductsDao.insert(record));
        }catch (Exception ex){
            if (ex.getMessage().indexOf("Duplicate entry") >= 0) {
                SeckillProducts existItem = findByProductPeriodKey(record.getProductPeriodKey());
                return existItem.getId();
            } else {
                log.error(ex.getMessage(), ex);
                throw new SecKillException(ex.getMessage());
            }
        }
    }

    @Override
    public SeckillProducts findByProductPeriodKey(String productPeriodKey) {
        Assert.isTrue(!StringUtils.isEmpty(productPeriodKey));

        SeckillProducts item = new SeckillProducts();
        item.setProductPeriodKey(productPeriodKey);
        List<SeckillProducts> list = seckillProductsDao.list(item);
        if (CollectionUtil.isEmpty(list)) return null;
        else return list.get(0);
    }
}