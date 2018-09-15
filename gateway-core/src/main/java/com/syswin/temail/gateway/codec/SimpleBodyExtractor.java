package com.syswin.temail.gateway.codec;

import io.netty.buffer.ByteBuf;

public class SimpleBodyExtractor implements BodyExtractor {

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    byte[] data = new byte[remainingBytes];
    byteBuf.readBytes(data);
    return data;
  }
}
