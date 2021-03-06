package com.syswin.temail.gateway.notify;


import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.ChannelHolder;
import io.netty.channel.Channel;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MessageHandler {

  private final ChannelHolder channelHolder;
  private final Gson gson;

  MessageHandler(ChannelHolder channelHolder) {
    this.channelHolder = channelHolder;
    this.gson = new GsonBuilder()
        .registerTypeAdapter(CDTPPacket.class, new PacketDeserializer())
        .create();
  }

  void onMessageReceived(String message) {
    try {
      log.debug("从MQ接受到消息: {}", message);
      CDTPPacket packet = gson.fromJson(message, CDTPPacket.class);
      CDTPHeader header = packet.getHeader();
      // 对于通知消息，重新生成packetId，避免跟请求的返回消息重复而产生错误
      header.setPacketId(UUID.randomUUID().toString());
      resetSignature(packet);

      String receiver = header.getReceiver();
      Iterable<Channel> channels = channelHolder.getChannels(receiver);
      for (Channel channel : channels) {
        log.debug("当前推送的通道信息：{}，推送的内容信息：{}", channel, packet);
        channel.writeAndFlush(packet, channel.voidPromise());
      }
    } catch (JsonSyntaxException e) {
      log.error("接收到不符合格式的消息：{}", message, e);
    }
  }
}
