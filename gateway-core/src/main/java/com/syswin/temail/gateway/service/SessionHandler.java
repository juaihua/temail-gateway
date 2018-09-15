package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.channel.Channel;

public interface SessionHandler {

  void login(Channel channel, CDTPPacket packet);

  void logout(Channel channel, CDTPPacket packet);

  void terminateChannel(Channel channel);
}
