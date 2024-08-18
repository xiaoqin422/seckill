package com.study.seckill.dao;

import java.util.List;
import com.study.seckill.model.SeckillProducts;
import com.study.seckill.common.util.page.bean.CommonQueryBean;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 
 * SeckillProducts数据库操作接口类
 * 
 **/

@Repository
public interface SeckillProductsDao{


	/**
	 * 
	 * 查询（根据主键ID查询）
	 * 
	 **/
	SeckillProducts  selectByPrimaryKey ( @Param("id") Long id );

	SeckillProducts selectForUpdate(Long productId);

	/**
	 * 
	 * 删除（根据主键ID删除）
	 * 
	 **/
	int deleteByPrimaryKey ( @Param("id") Long id );

	/**
	 * 
	 * 添加
	 * 
	 **/
	int insert( SeckillProducts record );

	/**
	 * 
	 * 修改 （匹配有值的字段）
	 * 
	 **/
	int updateByPrimaryKeySelective( SeckillProducts record );
	/**
	 * 乐观锁的方式扣减库存
	 *
	 * @param productId
	 * @return
	 */
	int updateStockByOptimistic(Long productId);
	/**
	 * 扣减库存
	 *
	 * @param productId
	 * @return
	 */
	int decrStock(Long productId);
	/**
	 * 
	 * list分页查询
	 * 
	 **/
	List<SeckillProducts> list4Page ( SeckillProducts record, @Param("commonQueryParam") CommonQueryBean query);

	/**
	 * 
	 * count查询
	 * 
	 **/
	int count ( SeckillProducts record);

	/**
	 * 
	 * list查询
	 * 
	 **/
	List<SeckillProducts> list ( SeckillProducts record);

}