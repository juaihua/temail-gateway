package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.channel.Channel;

public interface RequestHandler {

  void handleRequest(Channel channel, CDTPPacket packet);
}
