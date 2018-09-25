package com.syswin.temail.gateway.notify;

import static com.syswin.temail.gateway.encrypt.util.SignatureUtil.resetSignature;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.ps.server.service.ChannelHolder;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
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
        resetSignature(packet);
        log.info("当前推送的通道信息：{}，推送的内容信息：{}", channel, packet);
        channel.writeAndFlush(packet);
      }
    } catch (JsonSyntaxException e) {
      log.error("接收到不符合格式的消息：{}", message, e);
    }
  }
}
