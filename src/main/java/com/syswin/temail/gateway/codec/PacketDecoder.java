package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CDTPHeaderProto.CDTPHeader;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import com.syswin.temail.gateway.exception.PacketException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class PacketDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
      List<Object> list) throws Exception {
    // TODO(姚华成): 包长度的计算需要确认和修改
    int packetLength = byteBuf.readInt();
    if (packetLength <= 0) {
      throw new PacketException("包长度不合法：" + packetLength);
    }
    if (byteBuf.readableBytes() != packetLength) {
      throw new PacketException("无法读取到包长度指定的全部包数据：packetLength=" + packetLength
          + "，剩余可读取的数据长度" + byteBuf.readableBytes());
    }
    short commandSpace = byteBuf.readShort();
    if (commandSpace < 0) {
      throw new PacketException("命令空间不合法，commandSpace=" + commandSpace);
    }
    short command = byteBuf.readShort();
    if (command <= 0) {
      throw new PacketException("命令不合法，command=" + command);
    }
    short version = byteBuf.readShort();
    short headerLength = byteBuf.readShort();
    if (headerLength <= 0) {
      throw new PacketException("headerLength长度错误，headerLength=" + headerLength);
    }
    if (byteBuf.readableBytes() < headerLength) {
      throw new PacketException("无法读取到HeaderLength指定的全部Header数据：headerLength=" + headerLength
          + "，剩余可读取的数据长度" + byteBuf.readableBytes());
    }
    byte[] headerBytes = new byte[headerLength];
    byteBuf.readBytes(headerBytes);
    CDTPHeader cdtpHeader = CDTPHeader.parseFrom(headerBytes);
    byte[] data = new byte[packetLength - headerLength - 8];
    byteBuf.readBytes(data);

    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(commandSpace);
    packet.setCommand(command);
    packet.setVersion(version);
    packet.setHeader(Header.copyFrom(cdtpHeader));
    packet.setData(data);
    list.add(packet);
  }
}
