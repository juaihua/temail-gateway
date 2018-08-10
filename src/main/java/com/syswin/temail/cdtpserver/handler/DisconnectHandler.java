package com.syswin.temail.cdtpserver.handler;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;

import com.syswin.temail.cdtpserver.entity.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;

public class DisconnectHandler extends BaseHandler {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private AttributeKey<String> TEMAIL_KEY = AttributeKey.valueOf("TEMAIL_KEY");

  public DisconnectHandler(SocketChannel socketChannel, CDTPPackage cdtpPackage) {
    super(socketChannel, cdtpPackage);
  }

  @Override
  public void process() {  
    LOGGER.info("disconnection, close  SocketChannel {}",this.getSocketChannel().attr(TEMAIL_KEY).get());
   
  }

}
