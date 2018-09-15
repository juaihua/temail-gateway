package com.syswin.temail.gateway.handler;

import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError.Builder;
import com.syswin.temail.gateway.service.ChannelHolder;
import io.netty.channel.Channel;

class RequestInterceptor {

  private final ChannelHolder channelHolder;

  RequestInterceptor(ChannelHolder channelHolder) {
    this.channelHolder = channelHolder;
  }

  boolean isLoggedIn(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    if (!authSession(channel, temail, deviceId)) {
      CDTPPacket errorPacket = errorPacket(packet, INTERNAL_ERROR.getCode(), "用户" + temail + "在设备" + deviceId + "上没有登录，无法进行操作！");
      channel.writeAndFlush(errorPacket);
      return false;
    }
    return true;
  }

  private CDTPPacket errorPacket(CDTPPacket packet, int code, String message) {
    packet.setCommandSpace(CHANNEL.getCode());
    packet.setCommand(INTERNAL_ERROR.getCode());

    Builder builder = CDTPServerError.newBuilder();
    builder.setCode(code);
    builder.setDesc(message);
    packet.setData(builder.build().toByteArray());
    return packet;
  }

  private boolean authSession(Channel channel, String temail, String deviceId) {
    return isNotEmpty(temail)
        && isNotEmpty(deviceId)
        && channel == channelHolder.getChannel(temail, deviceId);
  }

  private boolean isNotEmpty(String str) {
    return str != null && str.length() > 0;
  }
}
