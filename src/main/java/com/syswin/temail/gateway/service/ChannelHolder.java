package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;

@Component
public class ChannelHolder {

  private final Map<String, Map<String, Channel>> sessionChannelMap = new ConcurrentHashMap<>();
  private final Map<Channel, Collection<Session>> channelSessionMap = new ConcurrentHashMap<>();

  public Channel getChannel(String temail, String deviceId) {
    return sessionChannelMap.getOrDefault(temail, Collections.emptyMap()).get(deviceId);
  }

  public Iterable<Channel> getChannels(String temail) {
    return sessionChannelMap.getOrDefault(temail, Collections.emptyMap()).values();
  }

  public boolean hasNoSession(Channel channel) {
    Collection<Session> sessions = channelSessionMap.get(channel);
    return sessions == null || sessions.isEmpty();
  }

  public void addSession(String temail, String deviceId, Channel channel) {
    Map<String, Channel> deviceChannelMap = sessionChannelMap.computeIfAbsent(temail, s -> new ConcurrentHashMap<>());
    Channel oldChannel = deviceChannelMap.put(deviceId, channel);
    if (!channel.equals(oldChannel)) {
      if (oldChannel != null) {
        removeChannelIfNoSession(temail, deviceId, oldChannel);
      }

      Collection<Session> sessions = channelSessionMap.computeIfAbsent(channel, s -> new ConcurrentLinkedQueue<>());
      sessions.add(new Session(temail, deviceId));
    }
  }

  public void removeSession(String temail, String deviceId) {
    Map<String, Channel> deviceChannelMap = sessionChannelMap.get(temail);
    if (deviceChannelMap != null) {
      Channel channel = deviceChannelMap.get(deviceId);
      // 先移除sessionChannel
      if (deviceChannelMap.size() > 1) {
        deviceChannelMap.remove(deviceId);
      } else {
        sessionChannelMap.remove(temail);
      }

      // 再移除channelSession
      removeChannelIfNoSession(temail, deviceId, channel);
    }
  }

  public Iterable<Session> removeChannel(Channel channel) {
    // 先移除channelSession
    Iterable<Session> sessions = channelSessionMap.remove(channel);
    // 再移除sessionChannel
    if (sessions != null) {
      for (Session session : sessions) {
        Map<String, Channel> deviceChannelMap = sessionChannelMap.get(session.getTemail());
        if (deviceChannelMap != null) {
          if (deviceChannelMap.size() > 1) {
            deviceChannelMap.remove(session.getDeviceId());
          } else {
            sessionChannelMap.remove(session.getTemail());
          }
        }
      }
    }
    return sessions;
  }

  private void removeChannelIfNoSession(String temail, String deviceId, Channel channel) {
    Collection<Session> sessions = channelSessionMap.getOrDefault(channel, Collections.emptyList());
    sessions.removeIf(session -> temail.equals(session.getTemail()) && deviceId.equals(session.getDeviceId()));
    if (sessions.isEmpty()) {
      channelSessionMap.remove(channel);
    }
  }
}
