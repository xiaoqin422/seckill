package com.study.seckill.controller;

import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.study.seckill.common.base.BaseResponse;
import com.study.seckill.common.base.Constant;
import com.study.seckill.model.vo.ImageCodeModel;
import com.study.seckill.common.util.cache.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 包名: com.study.seckill.controller
 * 类名: VerifyCodeController
 * 创建用户: 25789
 * 创建日期: 2022年10月29日 16:35
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Controller
@Slf4j
public class VerifyCodeController {
    @Autowired
    private DefaultKaptcha defaultKaptcha;
    @Autowired
    private RedisUtil redisUtil;
    @RequestMapping("/seckill/code")
    @ResponseBody
    public BaseResponse<ImageCodeModel> getNumCode(HttpServletResponse response, HttpServletRequest request) throws IOException {
        String imageId = IdUtil.objectId();
        // 获得二进制输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 通过DefaultKaptcha获得随机验证码
        String code = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(code);
        // 将图片写入到流中
        ImageIO.write(image, "jpg", baos);
        ImageCodeModel vo = new ImageCodeModel();
        vo.setImageId(imageId);
        vo.setImageStr(Base64.encodeBase64String(baos.toByteArray()));
        redisUtil.set(String.format(Constant.redisKey.SECKILL_IMAGE_CODE, imageId), code, 60);
        return BaseResponse.OK(vo);
    }
    @RequestMapping("/code/page")
    public String index() {
        return "code";
    }
}