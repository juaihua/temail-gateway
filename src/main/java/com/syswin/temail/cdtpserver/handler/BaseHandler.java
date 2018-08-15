package com.syswin.temail.cdtpserver.handler;

import com.syswin.temail.cdtpserver.TemailServerProperties;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;

import io.netty.channel.socket.SocketChannel;
import lombok.Data;

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
    
    private TemailServerProperties temailServerConfig;
   
    public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage){
      this.socketChannel = socketChannel;
      this.cdtpPackage = cdtpPackage;
    }
    
    public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage, TemailServerProperties temailServerConfig){
        this.socketChannel = socketChannel;
        this.cdtpPackage = cdtpPackage;
        this.temailServerConfig = temailServerConfig;
    }


    public abstract void process();
}
