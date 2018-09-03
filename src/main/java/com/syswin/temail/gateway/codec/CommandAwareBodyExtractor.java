package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CommandSpaceType;
import io.netty.buffer.ByteBuf;

class CommandAwareBodyExtractor implements BodyExtractor {

  private final BodyExtractor bodyExtractor;

  CommandAwareBodyExtractor(BodyExtractor bodyExtractor) {
    this.bodyExtractor = bodyExtractor;
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    int remaining = remainingBytes;
    if (isSendSingleMsg(commandSpace, command)) {
      // 单聊的消息比较特殊，把CDTP协议的整个数据打包编码后，放到Packet的Data里。
      int readableBytes = byteBuf.readableBytes();
      byteBuf.resetReaderIndex();
      remaining += byteBuf.readableBytes() - readableBytes;
    }
    return bodyExtractor.fromBuffer(commandSpace, command, byteBuf, remaining);
  }

  private boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == CommandSpaceType.SINGLE_MESSAGE.getCode() && command == 1;
  }
}
