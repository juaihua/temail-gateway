package com.syswin.temail.gateway.codec;

import io.netty.buffer.ByteBuf;

public interface BodyExtractor {

  byte[] invoke(short commandSpace, short command, ByteBuf byteBuf, int packetLength, short headerLength);
}
