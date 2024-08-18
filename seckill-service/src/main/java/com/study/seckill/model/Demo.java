package com.study.seckill.model;

import java.util.Date;


/**
 * 
 * 
 * 
 **/
public class Demo{


  /**主键**/

  private Long id;


  /****/

  private String text;


  /****/

  private Date createTime;


  /****/

  private Date updateTime;


  /****/

  private Integer isDeleted;


  /****/

  private Integer isEnabled;




  public void setId(Long id) {     this.id = id;
  }


  public Long getId() {     return this.id;
  }


  public void setText(String text) {     this.text = text;
  }


  public String getText() {     return this.text;
  }


  public void setCreateTime(Date createTime) {     this.createTime = createTime;
  }


  public Date getCreateTime() {     return this.createTime;
  }


  public void setUpdateTime(Date updateTime) {     this.updateTime = updateTime;
  }


  public Date getUpdateTime() {     return this.updateTime;
  }


  public void setIsDeleted(Integer isDeleted) {     this.isDeleted = isDeleted;
  }


  public Integer getIsDeleted() {     return this.isDeleted;
  }


  public void setIsEnabled(Integer isEnabled) {     this.isEnabled = isEnabled;
  }


  public Integer getIsEnabled() {     return this.isEnabled;
  }

}
