package com.syswin.temail.cdtpserver.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by weis on 18/8/7.
 *
 * <pre>
 * 自己定义的协议
 *  数据包格式
 * +——----——+——-----——+——----——+
 * |长度  |command  | version  |   数据       |
 * +——----——+——-----——+——----——+
 * 1.长度，为int类型的数据
 * 2.command , short类型
 * 3.version , short类型
 * 4.要传输的数据
 * </pre>
 */

public class EntityEncoder extends MessageToByteEncoder {

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf)
      throws Exception {

    // Packet packet = (Packet)o;
    // byte[] data = packet.getBody().toByteArray();
    //
    // byteBuf.writeInt(data.length);// write length
    // byteBuf.writeShort(packet.getFixedHeader().getCommand()); //write cmd
    // byteBuf.writeShort(packet.getFixedHeader().getVsersion()); //write version
    //
    // byteBuf.writeBytes(data);//write content

  }
}
