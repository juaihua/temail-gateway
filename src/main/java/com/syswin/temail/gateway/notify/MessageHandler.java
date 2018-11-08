package com.syswin.temail.gateway.notify;


import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.gateway.codec.CommandAwarePacketUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.server.service.ChannelHolder;
import io.netty.channel.Channel;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MessageHandler {

  private final ChannelHolder channelHolder;
  private final Gson gson = new Gson();
  private final CommandAwarePacketUtil packetUtil;

  MessageHandler(ChannelHolder channelHolder, CommandAwarePacketUtil packetUtil) {
    this.channelHolder = channelHolder;
    this.packetUtil = packetUtil;
  }

  void onMessageReceived(String message) {
    try {
      log.debug("从MQ接受到消息: {}", message);
      CDTPPacket packet = packetUtil.fromTrans(gson.fromJson(message, CDTPPacketTrans.class));
      CDTPHeader header = packet.getHeader();
      // 对于通知消息，重新生成packetId，避免跟请求的返回消息重复而产生错误
      header.setPacketId(UUID.randomUUID().toString());

      String receiver = header.getReceiver();
      Iterable<Channel> channels = channelHolder.getChannels(receiver);
      for (Channel channel : channels) {
        resetSignature(packet);
        log.debug("当前推送的通道信息：{}，推送的内容信息：{}", channel, packet);
        channel.writeAndFlush(packet);
      }
    } catch (JsonSyntaxException e) {
      log.error("接收到不符合格式的消息：{}", message, e);
    }
  }
}
