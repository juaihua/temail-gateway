package com.syswin.temail.cdtpserver.codec;

import lombok.extern.slf4j.Slf4j;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

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
 * */
@Slf4j
public class PacketEncoder extends MessageToByteEncoder<CDTPPackageProto.CDTPPackage>{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, CDTPPackageProto.CDTPPackage cdtpPackage, ByteBuf byteBuf) throws Exception {
      
        byte[] data = cdtpPackage.toByteArray();
        
        log.info("PacketEncoder length:"+data.length);
        byteBuf.writeInt(data.length);// write length
        byteBuf.writeBytes(data);
        
        //write content
//        if(o instanceof CDTPPackageProto.CDTPPackage){
//            CDTPPackageProto.CDTPPackage cdtpPackage = (CDTPPackageProto.CDTPPackage)o;
//            byte[] data = cdtpPackage.toByteArray();
//
//            byteBuf.writeInt(data.length);// write length
//            byteBuf.writeBytes(data);//write content
//        }
    }
}
