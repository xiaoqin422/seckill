package com.study.seckill.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class CommonWebUser implements Serializable {

    /****/
    private Long id;
    /****/
    private String email;
    /****/
    private String externalId;
    /****/
    private String phone;
    /****/
    private Date createTime;
    /****/
    private Date updateTime;

}
