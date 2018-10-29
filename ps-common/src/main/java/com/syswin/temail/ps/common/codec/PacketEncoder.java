package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PacketEncoder extends MessageToByteEncoder<CDTPPacket> {

  @Override
  public void encode(ChannelHandlerContext ctx,
      CDTPPacket packet, ByteBuf byteBuf) {
    byteBuf.writeBytes(PacketUtil.pack(packet));
    if (!packet.isHearbeat()) {
      log.debug("{}通道写入的信息是：CommandSpace={},Command={},CDTPHeader={},"
              + "Data={}", (ctx == null ? null : ctx.channel()), packet.getCommandSpace(), packet.getCommand(),
          packet.getHeader(),
          new String(packet.getData()));
    }
  }

}
