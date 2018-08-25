package com.syswin.temail.gateway.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
@AllArgsConstructor
public class TemailSocketInfo {

  // email账户
  private String account;
  // 操作类型 add: 新建链接， del: 用户登出或者心跳超时
  private String optype;
  private TemailSocketInstance status;

}
