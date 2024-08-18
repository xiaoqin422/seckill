package com.study.seckill.controller;

import com.study.seckill.model.SeckillAdmin;
import com.study.seckill.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 包名: com.study.seckill.controller
 * 类名: SeckillAdminController
 * 创建用户: 25789
 * 创建日期: 2022年10月09日 21:14
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Controller
@RequestMapping("/admin")
@Slf4j
public class SeckillAdminController {
    @Autowired
    private AdminService adminService;
    @RequestMapping("/listAdminPage")
    public String listAdminPage(Model model){
        List<SeckillAdmin> list = adminService.listAdmin();
        model.addAttribute("list",list);
        return "admin/listAdminPage";
    }
}