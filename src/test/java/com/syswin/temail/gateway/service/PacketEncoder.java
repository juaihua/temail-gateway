package com.syswin.temail.gateway.service;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.nio.ByteBuffer;

class PacketEncoder {

  byte[] encode(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();

    byte[] headerBytes;
    if (header != null) {
      headerBytes = header.toProtobufHeader().toByteArray();
    } else {
      headerBytes = new byte[0];
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
    byteBuffer
        .putInt(0)
        .putShort(packet.getCommandSpace())
        .putShort(packet.getCommand())
        .putShort(packet.getVersion())
        .putShort((short) headerBytes.length)
        .put(headerBytes)
        .put(packet.getData());

    int packetLength = byteBuffer.position();
    byteBuffer.putInt(0, packetLength - 4);
    byteBuffer.rewind();

    byte[] bytes = new byte[packetLength];
    byteBuffer.get(bytes);
    return bytes;
  }
}
