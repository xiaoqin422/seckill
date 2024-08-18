CREATE TABLE `seckill_admin` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `login_name` varchar(50) NOT NULL COMMENT '登录名',
    `password` varchar(100) DEFAULT NULL COMMENT '密码',
    `name` varchar(100) DEFAULT NULL COMMENT '展示管理员名称',
    `ip_range` varchar(256) DEFAULT NULL COMMENT 'ip范围，空不限制',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = innoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='管理员姓名表';

CREATE TABLE `seckill_products`
(
    `id`                 bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`               varchar(50)  DEFAULT NULL COMMENT '商品名称',
    `count`              int(11) DEFAULT NULL COMMENT '商品总库存量',
    `saled`              int(11) DEFAULT NULL COMMENT '已售数量',
    `create_time`        datetime     DEFAULT NULL COMMENT '创建时间',
    `is_deleted`         int(1) DEFAULT NULL COMMENT '是否删除：0否 1是',
    `start_buy_time`     datetime     DEFAULT NULL COMMENT '商品开始销售时间',
    `update_time`        datetime     DEFAULT NULL COMMENT '更新时间',
    `product_desc`       varchar(512) DEFAULT NULL COMMENT '商品简介',
    `status`             tinyint(4) DEFAULT NULL COMMENT '商品状态',
    `memo`               varchar(128) DEFAULT NULL COMMENT '备注信息',
    `product_period_key` varchar(32)  DEFAULT NULL COMMENT '唯一标识key',
    PRIMARY KEY (`id`),
    UNIQUE KEY `product_period_index`(`product_period_key`)
) ENGINE=innoDB DEFAULT CHARSET=utf8 COMMENT='商品表';

CREATE TABLE `seckill_order`(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
    `product_id` bigint(20) DEFAULT NULL COMMENT '商品id',
    `product_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `IDX_PID_UID`(`user_id`,`product_id`) USING BTREE
) ENGINE = innoDB DEFAULT CHARSET = utf8 COMMENT = '订单表';

CREATE TABLE `seckill_user`(
    `id` bigint(20) NOT NULL COMMENT '主键',
    `name` varchar(50) DEFAULT NULL COMMENT '用户名称',
    `phone` varchar(30) DEFAULT NULL COMMENT '用户手机号',
    `email` varchar(20) DEFAULT NULL COMMENT '用户邮箱',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `is_deleted` int(11) DEFAULT '0' COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`)
) ENGINE=innoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

CREATE TABLE `demo`(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `text` varchar(255) DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` int(1) DEFAULT NULL,
    `is_enabled` int(1) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = innoDB DEFAULT CHARSET = utf8;

CREATE TABLE `sso_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
    `external_id` varchar(32) not null COMMENT '外部ID',
    `phone` varchar(255) DEFAULT NULL COMMENT '手机号',
    `password` varchar(255) DEFAULT NULL COMMENT '密码',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `is_enabled` int(11) DEFAULT '1' COMMENT '是否启用',
    `is_deleted` int(11) DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
alter table `sso_user` add unique index `phone_index`(`phone`);
alter table `sso_user` add unique index `external_id_index`(`external_id`);