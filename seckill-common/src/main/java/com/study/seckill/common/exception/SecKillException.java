package com.study.seckill.common.exception;

import lombok.Data;

/**
 * 包名: com.study.seckill.common.exception
 * 类名: SecKillException
 * 创建用户: 25789
 * 创建日期: 2022年10月01日 19:32
 * 项目名: seckill
 *
 * @author: 秦笑笑
 **/
@Data
public class SecKillException extends RuntimeException{
    private static final long serialVersionUID = 8860225995376647065L;
    private String msg;
    private int code = 500;
    public SecKillException(String msg){
        super(msg);
        this.msg = msg;
    }
    public SecKillException(String msg, int code){
        super(msg);
        this.msg = msg;
        this.code = code;
    }
    public SecKillException(String msg, Throwable e){
        super(msg,e);
        this.msg = msg;
    }
    public SecKillException(String msg, int code,Throwable e){
        super(msg,e);
        this.msg = msg;
        this.code = code;
    }
}