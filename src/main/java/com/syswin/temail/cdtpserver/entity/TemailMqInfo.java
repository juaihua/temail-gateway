package com.syswin.temail.cdtpserver.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class TemailMqInfo implements Serializable {


  // 持有客户端链句柄的服务实例监听的消息队列topic
  private String mqTopic;

  // 持有客户端链句柄的服务实例监听的消息队列mqTag
  private String mqTag;


  // 持有客户端链句柄的服务实例宿主机地址
  private String hostOf;

  // 持有客户端链句柄的服务实例的进程号
  private String processId;


}
