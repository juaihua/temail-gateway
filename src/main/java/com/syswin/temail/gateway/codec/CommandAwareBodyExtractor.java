package com.syswin.temail.gateway.codec;

import static com.syswin.temail.gateway.codec.CommandAwarePacketUtil.isSendGroupMsg;
import static com.syswin.temail.gateway.codec.CommandAwarePacketUtil.isSendSingleMsg;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.utils.ByteBuf;

public class CommandAwareBodyExtractor implements BodyExtractor {

  private final BodyExtractor defaultBodyExtractor;
  private final TemailGatewayProperties properties;

  public CommandAwareBodyExtractor(BodyExtractor defaultBodyExtractor,
      TemailGatewayProperties properties) {
    this.defaultBodyExtractor = defaultBodyExtractor;
    this.properties = properties;
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    int remaining = remainingBytes;
    if (isSendSingleMsg(commandSpace, command) ||
        (isSendGroupMsg(commandSpace, command) && properties.isGroupPacketEnabled())) {
      // 单聊和群聊的消息比较特殊，把CDTP协议的整个数据打包编码后，放到Packet的Data里。
      int readableBytes = byteBuf.readableBytes();
      byteBuf.resetReaderIndex();
      remaining += byteBuf.readableBytes() - readableBytes;
    }
    return defaultBodyExtractor.fromBuffer(commandSpace, command, byteBuf, remaining);
  }

  @Override
  public void decrypt(CDTPPacket packet) {
    // 单聊消息无法也不需要解密
    if (!isSendSingleMsg(packet.getCommandSpace(), packet.getCommand())) {
      defaultBodyExtractor.decrypt(packet);
    }
  }
}
