package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.packet.ByteBuf;
import com.syswin.temail.ps.common.packet.PacketUtil;
import com.syswin.temail.ps.common.packet.SimplePacketUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CommandAwarePacketUtil extends PacketUtil implements BodyExtractor {

  private final BodyExtractor defaultBodyExtractor;
  private final PacketUtil defaultPacketUtil;
  private final CommandAwarePredicate commandAwarePredicate;

  public CommandAwarePacketUtil(TemailGatewayProperties properties) {
    this(SimpleBodyExtractor.INSTANCE, SimplePacketUtil.INSTANCE, new CommandAwarePredicate(properties));
  }

  public CommandAwarePacketUtil(BodyExtractor defaultBodyExtractor,
      PacketUtil defaultPacketUtil,
      CommandAwarePredicate commandAwarePredicate) {
    this.commandAwarePredicate = commandAwarePredicate;
    this.defaultBodyExtractor = defaultBodyExtractor;
    this.defaultPacketUtil = defaultPacketUtil;
  }

  @Override
  protected BodyExtractor getBodyExtractor() {
    return this;
  }

  public byte[] decodeData(CDTPPacketTrans packet, boolean original) {
    String data;
    if (packet == null || (data = packet.getData()) == null) {
      return new byte[0];
    }
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (commandAwarePredicate.check(commandSpace, command)) {
      byte[] dataBytes = Base64.getUrlDecoder().decode(data);
      if (original) {
        CDTPPacket originalPacket = unpack(dataBytes);
        return originalPacket.getData();
      } else {
        return dataBytes;
      }
    } else {
      return defaultPacketUtil.decodeData(packet);
    }
  }

  @Override
  public byte[] decodeData(CDTPPacketTrans packet) {
    return decodeData(packet, false);
  }

  public String encodeData(CDTPPacket packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (commandAwarePredicate.check(commandSpace, command)) {
      return Base64.getUrlEncoder().encodeToString(packet.getData());
    }
    return new String(packet.getData(), StandardCharsets.UTF_8);
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    int remaining = remainingBytes;
    if (commandAwarePredicate.check(commandSpace, command)) {
      // 单聊和群聊的消息比较特殊，把CDTP协议的整个数据打包编码后，放到Packet的Data里。
      int readableBytes = byteBuf.readableBytes();
      byteBuf.resetReaderIndex();
      remaining += byteBuf.readableBytes() - readableBytes;
    }
    return defaultBodyExtractor.fromBuffer(commandSpace, command, byteBuf, remaining);
  }

  public void decrypt(CDTPPacket packet) {
    // 单聊消息无法也不需要解密
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (!commandAwarePredicate.check(commandSpace, command)) {
      defaultBodyExtractor.decrypt(packet);
    }
  }
}
