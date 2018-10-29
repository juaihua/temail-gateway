package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PacketDecoder extends ByteToMessageDecoder {

  private final BodyExtractor bodyExtractor;

  public PacketDecoder() {
    this(new SimpleBodyExtractor());
  }

  public PacketDecoder(BodyExtractor bodyExtractor) {
    this.bodyExtractor = bodyExtractor;
  }

  @Override
  public void decode(ChannelHandlerContext ctx, ByteBuf byteBuf,
      List<Object> list) {
    byte[] bytes = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(bytes);
    CDTPPacket packet = PacketUtil.unpack(bytes, bodyExtractor);

    list.add(packet);
    if (!packet.isHearbeat() && log.isDebugEnabled()) {
      log.debug("{}通道读取的信息是：CommandSpace={},Command={},CDTPHeader={},"
              + "Data={}", (ctx == null ? null : ctx.channel()), packet.getCommandSpace(), packet.getCommand(),
          packet.getHeader(),
          new String(packet.getData()));
    }
  }
}
