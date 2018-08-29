package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CommandSpaceType;
import io.netty.buffer.ByteBuf;

class CommandAwareBodyExtractor implements BodyExtractor {

  private final BodyExtractor bodyExtractor;

  CommandAwareBodyExtractor(BodyExtractor bodyExtractor) {
    this.bodyExtractor = bodyExtractor;
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf) {
    if (isSendSingleMsg(commandSpace, command)) {
      // 单聊的消息比较特殊，把CDTP协议的整个数据打包编码后，放到Packet的Data里。
      byteBuf.resetReaderIndex();
    }
    return bodyExtractor.fromBuffer(commandSpace, command, byteBuf);
  }

  private boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == CommandSpaceType.SINGLE_MESSAGE.getCode() && command == 1;
  }
}
