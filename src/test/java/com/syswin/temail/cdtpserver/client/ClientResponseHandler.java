package com.syswin.temail.cdtpserver.client;

import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author 姚华成
 * @date 2018-8-25
 */

public class ClientResponseHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, CDTPPacket packet) {
    System.out.println(packet);
  }
}
