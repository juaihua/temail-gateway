package com.syswin.temail.ps.common.codec;

import io.netty.buffer.ByteBuf;

public interface BodyExtractor {

  byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes);
}
