package com.syswin.temail.cdtpserver.handler;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weis on 18/8/8.
 */
@Data
public abstract class BaseHandler {

 /*  @Setter
   @Getter
   private AttributeKey<String> TEMAIL_KEY = AttributeKey.valueOf("TEMAIL_KEY");*/

  
    private SocketChannel socketChannel;
    
    private CDTPPackageProto.CDTPPackage cdtpPackage;

    
    public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage){

        this.socketChannel = socketChannel;
        this.cdtpPackage = cdtpPackage;
       

    }


    public abstract void process();
}
