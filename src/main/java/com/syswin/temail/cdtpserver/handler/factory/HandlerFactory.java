package com.syswin.temail.cdtpserver.handler.factory;

import io.netty.channel.socket.SocketChannel;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.CommandEnum;
import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.handler.DisconnectHandler;
import com.syswin.temail.cdtpserver.handler.LoginHandler;
import com.syswin.temail.cdtpserver.handler.RequestHandler;
import com.syswin.temail.cdtpserver.handler.base.BaseHandler;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.status.TemailSocketSyncClient;

/**
 * Created by weis on 18/8/8.
 */
@Component
public class HandlerFactory {

  @Resource
  private TemailServerProperties temailServerConfig;

  @Resource
  private TemailSocketSyncClient temailSocketSyncClient;

  @Setter
  @Getter
  private TemailMqInfo temailMqInfo;


  public BaseHandler getHandler(CDTPPackageProto.CDTPPackage cdtpPackage,
      SocketChannel socketChannel) {
    if (cdtpPackage.getCommand() == CommandEnum.connect.getCode()) {
      return new LoginHandler(socketChannel, cdtpPackage, temailServerConfig,
          temailSocketSyncClient, temailMqInfo);
    } else if (cdtpPackage.getCommand() == CommandEnum.disconnect.getCode()) {
      return new DisconnectHandler(socketChannel, cdtpPackage, temailServerConfig);
    } else {
      return new RequestHandler(socketChannel, cdtpPackage, temailServerConfig);
    }
  }
}
