package com.syswin.temail.cdtpserver.network.codec;

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
 * +——----——+——-----——+——----——+
 * |长度  |command  | version  |   数据       |
 * +——----——+——-----——+——----——+
 * 1.长度，为int类型的数据
 * 2.command , short类型
 * 3.version , short类型
 * 4.要传输的数据
 * </pre>
 * */
public class EntityDecoder extends ByteToMessageDecoder {



    private final static int FIXED_HANDER_LENGTH = 8;


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        if(byteBuf.readableBytes() >= FIXED_HANDER_LENGTH){

            //防止字节流攻击,更具实际业务情况而定
            if(byteBuf.readableBytes() > 8192){
                byteBuf.skipBytes(byteBuf.readableBytes());
            }

            int readeStart;

            byteBuf.markReaderIndex();
            int dataLength = byteBuf.readInt();//读取消息总长度
            int countLength = dataLength + 4;//short command and short version
            if(dataLength < 0){
                //非法数据,关闭连接
                channelHandlerContext.close();
            }

            if(byteBuf.readableBytes() < countLength){
                byteBuf.resetReaderIndex();
                return;
            }

            //构建对象
//            Packet packet = new Packet();
//            FixedHeader fixedHeader = new FixedHeader();
//            fixedHeader.setLength(dataLength);
//            fixedHeader.setCommand(byteBuf.readShort());
//            fixedHeader.setVsersion(byteBuf.readShort());

            //protobuf
            byte[] protobufByte = new byte[dataLength];
            byteBuf.readBytes(protobufByte);


            CDTPPackageProto.CDTPPackage cdtpPackage = CDTPPackageProto.CDTPPackage.parseFrom(protobufByte);

//            packet.setFixedHeader(fixedHeader);
//            packet.setBody(cdtpPackage);
//
//            list.add(packet);

        }

    }
}
