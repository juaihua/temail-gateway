package com.syswin.temail.gateway.service;

import static com.syswin.temail.gateway.entity.CommandType.PONG;

import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.channel.Channel;

public class HeartBeatService {

  public void pong(Channel channel, CDTPPacket packet) {
    packet.setCommand(PONG.getCode());
    channel.writeAndFlush(packet);
  }
}
