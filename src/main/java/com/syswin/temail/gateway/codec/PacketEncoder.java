package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
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
@Component
public class PacketEncoder extends MessageToByteEncoder<CDTPPacket> {

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext,
      CDTPPacket packet, ByteBuf byteBuf) {
    byteBuf.writeShort(packet.getCommandSpace());
    byteBuf.writeShort(packet.getCommand());
    byteBuf.writeShort(packet.getVersion());
    Header header = packet.getHeader();
    byte[] headerBytes = header.toCDTPHeader().toByteArray();
    byteBuf.writeShort(headerBytes.length);
    byteBuf.writeBytes(headerBytes);
    byteBuf.writeBytes(packet.getData());
  }
}
