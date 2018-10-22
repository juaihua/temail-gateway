package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PacketEncoder extends MessageToByteEncoder<CDTPPacket> {

  @Override
  public void encode(ChannelHandlerContext ctx,
      CDTPPacket packet, ByteBuf byteBuf) {
    byteBuf.writeShort(packet.getCommandSpace());
    byteBuf.writeShort(packet.getCommand());
    byteBuf.writeShort(packet.getVersion());
    CDTPHeader header = packet.getHeader();
    if (header != null) {
      byte[] headerBytes = header.toCDTPHeader().toByteArray();
      byteBuf.writeShort(headerBytes.length);
      byteBuf.writeBytes(headerBytes);
    } else {
      byteBuf.writeShort(0);
    }
    byteBuf.writeBytes(packet.getData());
    if (!packet.isHearbeat()) {
      log.debug("{}通道写入的信息是：CommandSpace={},Command={},CDTPHeader={},"
              + "Data={}", ctx.channel(), packet.getCommandSpace(), packet.getCommand(), packet.getHeader(),
          new String(packet.getData()));
    }
  }

}
