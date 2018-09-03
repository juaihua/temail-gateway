package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CDTPHeader;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf;
import com.syswin.temail.gateway.exception.PacketException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PacketDecoder extends ByteToMessageDecoder {

  private final BodyExtractor bodyExtractor;

  public PacketDecoder() {
    this(new CommandAwareBodyExtractor(new SimpleBodyExtractor()));
  }

  public PacketDecoder(BodyExtractor bodyExtractor) {
    this.bodyExtractor = bodyExtractor;
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
      List<Object> list) throws Exception {
    CDTPPacket packet = new CDTPPacket();

    int packetLength = byteBuf.readInt();
    if (packetLength <= 0) {
      throw new PacketException("包长度不合法：" + packetLength);
    }
    if (byteBuf.readableBytes() < packetLength) {
      byteBuf.resetReaderIndex();
      return;
    }
    byteBuf.markReaderIndex();
    short commandSpace = byteBuf.readShort();
    if (commandSpace < 0) {
      throw new PacketException("命令空间不合法，commandSpace=" + commandSpace);
    }
    packet.setCommandSpace(commandSpace);

    short command = byteBuf.readShort();
    if (command <= 0) {
      throw new PacketException("命令不合法，command=" + command);
    }
    packet.setCommand(command);

    short version = byteBuf.readShort();
    packet.setVersion(version);

    short headerLength = byteBuf.readShort();
    CDTPProtoBuf.CDTPHeader cdtpHeader;
    if (headerLength < 0) {
      throw new PacketException("headerLength长度错误，headerLength=" + headerLength);
    }
    if (headerLength > 0) {
      if (byteBuf.readableBytes() < headerLength) {
        throw new PacketException("无法读取到HeaderLength指定的全部Header数据：headerLength=" + headerLength
            + "，剩余可读取的数据长度" + byteBuf.readableBytes());
      }
      byte[] headerBytes = new byte[headerLength];
      byteBuf.readBytes(headerBytes);
      cdtpHeader = CDTPProtoBuf.CDTPHeader.parseFrom(headerBytes);
      packet.setHeader(new CDTPHeader(cdtpHeader));
    }

    byte[] data = bodyExtractor.fromBuffer(commandSpace, command, byteBuf, packetLength - headerLength - 8);

    packet.setData(data);
    list.add(packet);
    log.info("从通道读取的信息是：CommandSpace={},Command={},CDTPHeader={},"
        + "Data={}", packet.getCommandSpace(), packet.getCommand(), packet.getHeader(), new String(packet.getData()));
  }

}
