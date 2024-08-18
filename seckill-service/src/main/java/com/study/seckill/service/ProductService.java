package com.study.seckill.service;

import com.study.seckill.common.util.page.bean.CommonQueryBean;
import com.study.seckill.model.SeckillProducts;

import java.util.List;

/**
 * 包名: com.study.seckill.service
 * 类名: ProductService
 * 创建用户: 25789
 * 创建日期: 2022年10月09日 20:56
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
public interface ProductService {
    /**
     *
     * 查询（根据主键ID查询）
     *
     **/
    SeckillProducts  selectByPrimaryKey(Long id);
    /**
     *
     * 添加
     *
     **/
    int insert(SeckillProducts record);
    /**
     *
     * 修改 （匹配有值的字段）
     *
     **/
    int updateByPrimaryKeySelective(SeckillProducts record);
    /**
     *
     * list分页查询
     *
     **/
    List<SeckillProducts> list4Page(SeckillProducts record, CommonQueryBean query);
    /**
     *
     * count查询
     *
     **/
    long count(SeckillProducts record);
    /**
     *
     * list查询
     *
     **/
    List<SeckillProducts> list(SeckillProducts record);
    /**
     * 唯一索引保证新增的数据唯一.
     */
    Long uniqueInsert(SeckillProducts record);
    /**
     *
     */
    SeckillProducts findByProductPeriodKey(String productPeriodKey);
}
