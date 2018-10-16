package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf;
import com.syswin.temail.ps.common.exception.PacketException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PacketDecoder extends ByteToMessageDecoder {

  private final BodyExtractor bodyExtractor;
  private final boolean autoDecrypt;

  public PacketDecoder() {
    this(false);
  }

  public PacketDecoder(BodyExtractor bodyExtractor) {
    this(bodyExtractor, false);
  }

  public PacketDecoder(boolean autoDecrypt) {
    this(new SimpleBodyExtractor(), autoDecrypt);
  }

  public PacketDecoder(BodyExtractor bodyExtractor, boolean autoDecrypt) {
    this.bodyExtractor = bodyExtractor;
    this.autoDecrypt = autoDecrypt;
  }

  @Override
  public void decode(ChannelHandlerContext ctx, ByteBuf byteBuf,
      List<Object> list) throws Exception {
    CDTPPacket packet = new CDTPPacket();

    byteBuf.markReaderIndex();
    int packetLength = byteBuf.readInt();
    if (packetLength <= 0) {
      throw new PacketException("包长度不合法：" + packetLength);
    }
    if (byteBuf.readableBytes() < packetLength) {
      byteBuf.resetReaderIndex();
      return;
    }
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

    if (autoDecrypt) {
      bodyExtractor.decrypt(packet);
    }

    list.add(packet);
    if (!packet.isHearbeat()) {
      log.debug("{}通道读取的信息是：CommandSpace={},Command={},CDTPHeader={},"
              + "Data={}", ctx.channel(), packet.getCommandSpace(), packet.getCommand(), packet.getHeader(),
          new String(packet.getData()));
    }
  }
}
