package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.buffer.ByteBuf;

public interface BodyExtractor {

  byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes);

  default void decrypt(CDTPPacket packet) {
  }

}
