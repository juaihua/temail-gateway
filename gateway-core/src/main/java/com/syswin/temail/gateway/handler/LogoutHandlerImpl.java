package com.syswin.temail.gateway.handler;

import com.syswin.temail.gateway.entity.CDTPHeader;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.service.ChannelCollector;
import io.netty.channel.Channel;

class LogoutHandlerImpl implements LogoutHandler {

  private final Channel channel;
  private final ChannelCollector channelCollector;

  LogoutHandlerImpl(ChannelCollector channelCollector, Channel channel) {
    this.channel = channel;
    this.channelCollector = channelCollector;
  }

  @Override
  public void onSucceeded(CDTPPacket request, CDTPPacket response) {
    channel.writeAndFlush(response);

    CDTPHeader header = request.getHeader();
    channelCollector.removeSession(header.getSender(), header.getDeviceId(), channel);
  }
}
