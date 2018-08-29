package com.syswin.temail.gateway.codec;

import io.netty.buffer.ByteBuf;

public interface BodyExtractor {

  byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf);
}
