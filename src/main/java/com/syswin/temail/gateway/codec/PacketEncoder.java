package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CDTPHeader;
import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by weis on 18/8/7.
 *
 * <pre>
 * 自己定义的协议
 *  数据包格式
 * +——--------——+
 * |长度  | 数据 |
 * +——--------——+
 * 1.长度，为int类型的数据
 * 2.command , short类型
 * 3.version , short类型
 * 4.要传输的数据
 * </pre>
 */
@Slf4j
@Component
public class PacketEncoder extends MessageToByteEncoder<CDTPPacket> {

  @Override
  protected void encode(ChannelHandlerContext ctx,
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

    log.info("写入通道{}的信息是：CommandSpace={},Command={},CDTPHeader={},"
            + "Data={}", ctx.channel(), packet.getCommandSpace(), packet.getCommand(), packet.getHeader(),
        new String(packet.getData()));
  }
}
