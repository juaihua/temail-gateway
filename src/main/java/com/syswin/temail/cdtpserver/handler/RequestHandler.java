package com.syswin.temail.cdtpserver.handler;

import io.netty.channel.socket.SocketChannel;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.syswin.temail.cdtpserver.constants.ConstantsAttributeKey;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;
import com.syswin.temail.cdtpserver.handler.base.BaseHandler;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;

public class RequestHandler extends BaseHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());

  public RequestHandler(SocketChannel socketChannel, CDTPPackage cdtpPackage,
      TemailServerProperties temailServerConfig) {
    super(socketChannel, cdtpPackage, temailServerConfig);
  }

  @Override
  public void process() {

    LOGGER.info("收到转发消息 :{}", getCdtpPackage().toString());
    TransferCDTPPackage transferCDTPPackage = new TransferCDTPPackage();

    BeanUtils.copyProperties(getCdtpPackage(), transferCDTPPackage);
    transferCDTPPackage.setData(getCdtpPackage().getData().toString(Charset.defaultCharset()));

    // transferCDTPPackage.setData(getCdtpPackage().getData().toString());
    HttpAsyncClient.post(getTemailServerConfig().getDispatchUrl(), transferCDTPPackage,
        getSocketChannel());
    LOGGER.info("执行dispath commond成功, the temail key is {}",
        this.getSocketChannel().attr(ConstantsAttributeKey.TEMAIL_KEY).get());
  }

}
