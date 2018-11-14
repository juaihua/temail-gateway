package com.syswin.temail.gateway.codec;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

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

/**
 * @author 姚华成
 * @date 2018-10-30
 */
public class CommandAwarePacketUtil extends PacketUtil implements BodyExtractor {

  private final TemailGatewayProperties properties;
  private final BodyExtractor defaultBodyExtractor;
  private final PacketUtil defaultPacketUtil;

  public CommandAwarePacketUtil(TemailGatewayProperties properties) {
    this(properties, SimpleBodyExtractor.INSTANCE, SimplePacketUtil.INSTANCE);
  }

  public CommandAwarePacketUtil(TemailGatewayProperties properties, BodyExtractor defaultBodyExtractor,
      PacketUtil defaultPacketUtil) {
    this.properties = properties;
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
    if (isSendSingleMsg(commandSpace, command) ||
        isSendGroupMsg(commandSpace, command)) {
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
    if (isSendSingleMsg(commandSpace, command) ||
        isSendGroupMsg(commandSpace, command)) {
      return Base64.getUrlEncoder().encodeToString(packet.getData());
    } else {
      return new String(packet.getData(), StandardCharsets.UTF_8);
    }
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    int remaining = remainingBytes;
    if (isSendSingleMsg(commandSpace, command) ||
        (isSendGroupMsg(commandSpace, command))) {
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
    if (!isSendSingleMsg(commandSpace, command) &&
        (!isSendGroupMsg(commandSpace, command))) {
      defaultBodyExtractor.decrypt(packet);
    }
  }

  public boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 1;
  }

  public boolean isSendGroupMsg(short commandSpace, short command) {
    return (commandSpace == GROUP_MESSAGE_CODE && command == 1) &&
        properties.isGroupPacketEnabled();
  }

  public boolean isGroupJoin(short commandSpace, short command) {
    return commandSpace == 2 && command == 0x0107;
  }

}
