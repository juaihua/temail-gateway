package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CommandSpaceType;
import io.netty.buffer.ByteBuf;

class CommandAwareBodyExtractor implements BodyExtractor {

  public CommandAwareBodyExtractor() {
  }

  @Override
  public byte[] invoke(short commandSpace, short command, ByteBuf byteBuf, int packetLength, short headerLength) {
    byte[] data;
    if (isSendSingleMsg(commandSpace, command)) {
      // 单聊的消息比较特殊，把CDTP协议的整个数据打包编码后，放到Packet的Data里。
      byteBuf.resetReaderIndex();
      data = new byte[packetLength];
      byteBuf.readBytes(data);
    } else {
      data = new byte[packetLength - headerLength - 8];
      byteBuf.readBytes(data);
    }
    return data;
  }

  private boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == CommandSpaceType.SINGLE_MESSAGE.getCode() && command == 1;
  }
}
