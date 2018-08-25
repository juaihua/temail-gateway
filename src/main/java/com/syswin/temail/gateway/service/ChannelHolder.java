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

  public List<Session> getSessions(Channel channel) {
    return Collections.unmodifiableList(channelSessionMap.getOrDefault(channel, Collections.emptyList()));
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
      List<Session> sessions = getSessions(channel);
      if (sessions.isEmpty()) {
        channel.close();
      }
      if (deviceChannelMap.size() > 1) {
        deviceChannelMap.remove(deviceId);
      } else {
        sessionChannelMap.remove(temail);
      }
    }
  }

  public List<Session> removeChannel(Channel channel) {
    List<Session> sessions = channelSessionMap.remove(channel);
    if (sessions != null) {
      for (Session session : sessions) {
        removeSession(session.getTemail(), session.getDeviceId());
      }
    }
    return sessions;
  }
}
