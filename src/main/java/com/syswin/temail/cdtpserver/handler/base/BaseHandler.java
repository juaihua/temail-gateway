package com.syswin.temail.cdtpserver.handler.base;

import io.netty.channel.socket.SocketChannel;
import lombok.Data;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
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
  

  public abstract void process();
}
