package com.syswin.temail.cdtpserver.entity;

import lombok.Data;

@Data
public class TemailSocketInstance {

    //移动端设备id
    private  String  devId;
    //持有客户端链句柄的服务实例宿主机地址    
    private  String  hostOf;
    //持有客户端链句柄的服务实例的进程号 
    private  String  processId;
    //持有客户端链句柄的服务实例监听的消息队列topic 
    private  String  mqTopic;
    
}
