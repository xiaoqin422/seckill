package com.study.seckill.common.base;

/**
 * 包名: com.study.seckill.util
 * 类名: Constant
 * 创建用户: 25789
 * 创建日期: 2022年10月14日 16:07
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
public interface Constant {
    String FAIL = "FAIL";
    String SUCCESS = "SUCCESS";

    String VALIDATE_CODE_SALT = "bmw384919";
    long VISIT_LIMIT = 1;
    long CODE_LIMIT = 5;
    interface redisKey {
        /**
         * 短信验证码 u:p:c:+phone
         */
        String USER_PHONE_CODE = "u:p:c:%s";
        /**
         * 邮箱验证码 u:e:c:%s+email
         */
        String USER_EMAIL_CODE = "u:e:c:%s";
        /**
         * 手机号请求频次 u:p:c:p:+phone
         */
        String USER_PHONE_CODE_PINCI = "u:p:c:p:%s";
        /**
         * 手机号请求次数 u:p:c:v:+phone
         */
        String USER_PHONE_CODE_VISIT = "u:p:c:v:%s";
        /**
         * 邮箱请求频次 u:e:c:p:+email
         */
        String USER_EMAIL_CODE_PINCI = "u:p:c:p:%s";
        /**
         * 邮箱请求次数 u:e:c:v:+email
         */
        String USER_EMAIL_CODE_VISIT = "u:e:c:v:%s";
        /**
         * 分布式锁的KEY
         * sk:d:lock:商品id
         */
        String SECKILL_DISTRIBUTED_LOCK = "sk:d:lock:%s";

        /**
         * 缓存库存数量 + 商品id
         * sk:sc:商品id
         */
        String SECKILL_SALED_COUNT = "sk:sc:%s";

        /**
         * 已购买用户名单 + 商品id
         * sk:ou:p:商品id
         */
        String SECKILL_ORDERED_USER = "sk:ou:p:%s";

        /**
         * 秒杀验证码
         * sk:p:商品id:u:用户id
         */
        String SECKILL_VALIDATE_CODE = "sk:p:%s:u:%s";

        /**
         * 图片验证码（1分钟过期）
         * sk:i:{ImageId}
         */
        String SECKILL_IMAGE_CODE = "sk:i:%s";
        /**
         * 访问用户名单 + 商品id + 用户id
         * sk:v:p:商品id:u:用户id
         */
        String SECKILL_USER_VISIT = "sk:v:p:%s:u:%s";
        /**
         * 秒杀商品信息 sk:sc:p:商品ID
         */
        String SECKILL_PRODUCT_INFO = "sk:sc:p:%s";
    }

}
