package com.syswin.temail.gateway.codec;

import io.netty.buffer.ByteBuf;

public class PlainBodyExtractor implements BodyExtractor {

  @Override
  public byte[] invoke(short commandSpace, short command, ByteBuf byteBuf, int packetLength, short headerLength) {
    byte[] data = new byte[packetLength - headerLength - 8];
    byteBuf.readBytes(data);
    return data;
  }
}
