package com.syswin.temail.gateway.codec;

import static com.syswin.temail.gateway.codec.CommandAwarePacketUtil.isSendGroupMsg;
import static com.syswin.temail.gateway.codec.CommandAwarePacketUtil.isSendSingleMsg;

import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.utils.ByteBuf;

public class CommandAwareBodyExtractor implements BodyExtractor {

  private final BodyExtractor bodyExtractor;

  public CommandAwareBodyExtractor(BodyExtractor bodyExtractor) {
    this.bodyExtractor = bodyExtractor;
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    int remaining = remainingBytes;
    if (isSendSingleMsg(commandSpace, command) || isSendGroupMsg(commandSpace, command)) {
      // 单聊和群聊的消息比较特殊，把CDTP协议的整个数据打包编码后，放到Packet的Data里。
      int readableBytes = byteBuf.readableBytes();
      byteBuf.resetReaderIndex();
      remaining += byteBuf.readableBytes() - readableBytes;
    }
    return bodyExtractor.fromBuffer(commandSpace, command, byteBuf, remaining);
  }

  @Override
  public void decrypt(CDTPPacket packet) {
    // 单聊消息无法也不需要解密
    if (!isSendSingleMsg(packet.getCommandSpace(), packet.getCommand())) {
      bodyExtractor.decrypt(packet);
    }
  }
}
