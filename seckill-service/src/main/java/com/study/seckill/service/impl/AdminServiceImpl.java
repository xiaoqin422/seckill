package com.study.seckill.service.impl;

import com.study.seckill.dao.SeckillAdminDao;
import com.study.seckill.model.SeckillAdmin;
import com.study.seckill.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 包名: com.study.seckill.service.impl
 * 类名: AdminServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年10月09日 21:13
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private SeckillAdminDao seckillAdminDao;

    @Override
    public List<SeckillAdmin> listAdmin() {
        return seckillAdminDao.list(new SeckillAdmin());
    }
}