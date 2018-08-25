package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.Session;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Component
public class ChannelHolder {

  private Map<String, Map<String, Channel>> sessionChannelMap = new ConcurrentHashMap<>();
  private Map<Channel, List<Session>> channelSessionMap = new ConcurrentHashMap<>();

  public Channel getChannel(String temail, String deviceId) {
    return sessionChannelMap.getOrDefault(temail, Collections.emptyMap()).get(deviceId);
  }

  public Collection<Channel> getChannels(String temail) {
    return Collections.unmodifiableCollection(sessionChannelMap.getOrDefault(temail, Collections.emptyMap()).values());
  }

  public boolean hasNoSession(Channel channel) {
    List<Session> sessions = channelSessionMap.get(channel);
    return sessions == null || sessions.isEmpty();
  }

  public void addSession(String temail, String deviceId, Channel channel) {
    Map<String, Channel> deviceChannelMap = sessionChannelMap.computeIfAbsent(temail, s -> new ConcurrentHashMap<>());
    Channel oldChannel = deviceChannelMap.put(deviceId, channel);
    if (oldChannel != null && !oldChannel.equals(channel)) {
      List<Session> sessions = channelSessionMap.get(oldChannel);
      sessions.removeIf(session -> temail.equals(session.getTemail()) && deviceId.equals(session.getDeviceId()));
      if (sessions.isEmpty()) {
        channelSessionMap.remove(oldChannel);
      }
    }

    List<Session> sessions = channelSessionMap.computeIfAbsent(channel, s -> new ArrayList<>());
    sessions.add(new Session(temail, deviceId));
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
      List<Session> sessions = getSessions(channel);
      sessions.removeIf(session -> temail.equals(session.getTemail()) && deviceId.equals(session.getDeviceId()));
      if (sessions.isEmpty()) {
        channelSessionMap.remove(channel);
      }
    }
  }

  public List<Session> removeChannel(Channel channel) {
    // 先移除channelSession
    List<Session> sessions = channelSessionMap.remove(channel);
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

  private List<Session> getSessions(Channel channel) {
    return channelSessionMap.getOrDefault(channel, Collections.emptyList());
  }
}
