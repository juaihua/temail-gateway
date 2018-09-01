package com.syswin.temail.gateway.notify;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacketTrans;
import com.syswin.temail.gateway.service.ChannelHolder;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MessageHandler {

  private final ChannelHolder channelHolder;
  private final Gson gson;

  MessageHandler(ChannelHolder channelHolder) {
    this.channelHolder = channelHolder;
    this.gson = new Gson();
  }

  void onMessageReceived(String message) {
    try {
      log.info("从MQ接受到消息: {}", message);
      CDTPPacket packet = gson.fromJson(message, CDTPPacketTrans.class).toCDTPPacket();

      String receiver = packet.getHeader().getReceiver();
      Iterable<Channel> channels = channelHolder.getChannels(receiver);
      for (Channel channel : channels) {
        channel.writeAndFlush(packet);
      }
    } catch (JsonSyntaxException e) {
      log.error("接收到不符合格式的消息：{}", message, e);
    }
  }
}
