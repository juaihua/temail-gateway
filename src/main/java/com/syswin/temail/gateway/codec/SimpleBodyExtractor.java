package com.syswin.temail.gateway.codec;

import io.netty.buffer.ByteBuf;

public class SimpleBodyExtractor implements BodyExtractor {

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf) {
    byte[] data = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(data);
    return data;
  }
}
