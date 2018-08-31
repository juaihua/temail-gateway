package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.Session;
import io.netty.channel.Channel;

public interface ChannelCollector {

  boolean hasNoSession(Channel channel);

  void addSession(String temail, String deviceId, Channel channel);

  void removeSession(String temail, String deviceId, Channel channel);

  Iterable<Session> removeChannel(Channel channel);
}
