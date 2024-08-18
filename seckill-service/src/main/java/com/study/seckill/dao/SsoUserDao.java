package com.study.seckill.dao;

import java.util.List;

import com.study.seckill.common.util.page.bean.CommonQueryBean;
import com.study.seckill.model.SeckillUser;
import com.study.seckill.model.SsoUser;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 
 * SsoUser数据库操作接口类
 * 
 **/

@Repository
public interface SsoUserDao{


	/**
	 * 
	 * 查询（根据主键ID查询）
	 * 
	 **/
	SsoUser  selectByPrimaryKey ( @Param("id") Long id );

	/**
	 * 查询（根据用户邮箱）
	 * @param email 用户邮箱
	 */
	SsoUser selectByEmail(String email);

	/**
	 * 查询（根据用户手机）
	 * @param phone 用户手机
	 */
	SsoUser selectByPhone(String phone);

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
	int insert( SsoUser record );

	/**
	 * 
	 * 修改 （匹配有值的字段）
	 * 
	 **/
	int updateByPrimaryKeySelective( SsoUser record );

	/**
	 * 
	 * list分页查询
	 * 
	 **/
	List<SsoUser> list4Page ( SsoUser record, @Param("commonQueryParam") CommonQueryBean query);

	/**
	 * 
	 * count查询
	 * 
	 **/
	int count (SsoUser record);

	/**
	 * 
	 * list查询
	 * 
	 **/
	List<SsoUser> list ( SsoUser record);

}