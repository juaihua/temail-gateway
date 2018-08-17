package com.syswin.temail.cdtpserver.handler.base;

import io.netty.channel.socket.SocketChannel;
import lombok.Data;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.status.TemailSocketSyncClient;

/**
 * Created by weis on 18/8/8.
 */
@Data
public abstract class BaseHandler {

  private SocketChannel socketChannel;

  private CDTPPackageProto.CDTPPackage cdtpPackage;

  private TemailServerProperties temailServerConfig;

  private TemailSocketSyncClient temailSocketSyncClient;
  
  private  TemailMqInfo  temailMqInfo;

  
  public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage) {
    this.socketChannel = socketChannel;
    this.cdtpPackage = cdtpPackage;
  }

  
 
  public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage,
      TemailServerProperties temailServerConfig) {
    this.socketChannel = socketChannel;
    this.cdtpPackage = cdtpPackage;
    this.temailServerConfig = temailServerConfig;
  }

  
  public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage,
      TemailServerProperties temailServerConfig, TemailSocketSyncClient temailSocketSyncClient) {
    this.socketChannel = socketChannel;
    this.cdtpPackage = cdtpPackage;
    this.temailServerConfig = temailServerConfig;
    this.temailSocketSyncClient = temailSocketSyncClient;
  }
  
  public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage,
      TemailServerProperties temailServerConfig, TemailSocketSyncClient temailSocketSyncClient, TemailMqInfo  temailMqInfo) {
    this.socketChannel = socketChannel;
    this.cdtpPackage = cdtpPackage;
    this.temailServerConfig = temailServerConfig;
    this.temailSocketSyncClient = temailSocketSyncClient;
    this.temailMqInfo = temailMqInfo;
  }
  

  public abstract void process();
}
