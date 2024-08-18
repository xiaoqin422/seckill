package com.study.seckill.model;

import java.util.Date;


/**
 * 
 * 
 * 
 **/
public class SsoUser{


  /**主键**/

  private Long id;


  /**邮箱**/

  private String email;


  /**外部ID**/

  private String externalId;


  /**手机号**/

  private String phone;


  /**密码**/

  private String password;


  /**创建时间**/

  private Date createTime;


  /**更新时间**/

  private Date updateTime;


  /**是否启用**/

  private Integer isEnabled;


  /**是否删除**/

  private Integer isDeleted;




  public void setId(Long id) {     this.id = id;
  }


  public Long getId() {     return this.id;
  }


  public void setEmail(String email) {     this.email = email;
  }


  public String getEmail() {     return this.email;
  }


  public void setExternalId(String externalId) {     this.externalId = externalId;
  }


  public String getExternalId() {     return this.externalId;
  }


  public void setPhone(String phone) {     this.phone = phone;
  }


  public String getPhone() {     return this.phone;
  }


  public void setPassword(String password) {     this.password = password;
  }


  public String getPassword() {     return this.password;
  }


  public void setCreateTime(Date createTime) {     this.createTime = createTime;
  }


  public Date getCreateTime() {     return this.createTime;
  }


  public void setUpdateTime(Date updateTime) {     this.updateTime = updateTime;
  }


  public Date getUpdateTime() {     return this.updateTime;
  }


  public void setIsEnabled(Integer isEnabled) {     this.isEnabled = isEnabled;
  }


  public Integer getIsEnabled() {     return this.isEnabled;
  }


  public void setIsDeleted(Integer isDeleted) {     this.isDeleted = isDeleted;
  }


  public Integer getIsDeleted() {     return this.isDeleted;
  }

}
