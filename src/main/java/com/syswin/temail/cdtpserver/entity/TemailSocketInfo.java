package com.syswin.temail.cdtpserver.entity;

import lombok.Data;

@Data
public class TemailSocketInfo {
  
    //email账户    
    private  String  account;
    //操作类型   add: 新建链接， del: 用户登出或者心跳超时
    private  String  optype; 
    private  TemailSocketInstance  instance;
  
}
