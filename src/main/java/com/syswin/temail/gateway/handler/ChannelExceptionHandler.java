package com.syswin.temail.gateway.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Component
public class ChannelExceptionHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // TODO(姚华成):将错误返回客户端

  }

}
