package com.syswin.temail.cdtpserver.codec;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by weis on 18/8/7.
 *
 * <pre>
 * 自己定义的协议
 *  数据包格式
 * +——----——+——---——+
 * |长度  |   数据              |
 * +——----——+——---——+
 * 1.长度，为int类型的数据
 * 2.要传输的数据
 * </pre>
 */
public class PacketDecoder extends ByteToMessageDecoder {


  private final static int FIXED_HANDER_LENGTH = 4;


  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
      List<Object> list) throws Exception {

    if (byteBuf.readableBytes() < FIXED_HANDER_LENGTH) {
      return;
    }

    byteBuf.markReaderIndex();

    int dataLength = byteBuf.readInt();
    if (dataLength < 0) {
      channelHandlerContext.close();
    }

    if (byteBuf.readableBytes() < dataLength) {
      byteBuf.resetReaderIndex();
      return;
    }
    // protobuf
    byte[] protobufByte = new byte[dataLength];
    byteBuf.readBytes(protobufByte);

    CDTPPackageProto.CDTPPackage cdtpPackage = CDTPPackageProto.CDTPPackage.parseFrom(protobufByte);

    list.add(cdtpPackage);
    // if(byteBuf.readableBytes() >= FIXED_HANDER_LENGTH){
    //
    // //防止字节流攻击,更具实际业务情况而定
    // if(byteBuf.readableBytes() > 8192){
    // byteBuf.skipBytes(byteBuf.readableBytes());
    // }
    //
    // int readerIndex = byteBuf.readerIndex();
    //
    // byteBuf.markReaderIndex();
    // int dataLength = byteBuf.readInt();//读取消息总长度
    //
    // if(dataLength < 0){
    // //非法数据,关闭连接
    // channelHandlerContext.close();
    // }
    //
    // if(byteBuf.readableBytes() < dataLength){
    // byteBuf.resetReaderIndex();
    // // byteBuf.readerIndex(readerIndex);
    // return;
    // }
    //
    // //构建对象
    //
    // //protobuf
    // byte[] protobufByte = new byte[dataLength];
    // byteBuf.readBytes(protobufByte);
    //
    //
    // CDTPPackageProto.CDTPPackage cdtpPackage =
    // CDTPPackageProto.CDTPPackage.parseFrom(protobufByte);
    //
    //
    // list.add(cdtpPackage);
    //
    // }

  }
}
