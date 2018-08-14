package com.syswin.temail.cdtpserver.handler;

import java.sql.Connection;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.utils.CommandEnum;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by weis on 18/8/8.
 */
public class HandlerFactory {

    public static BaseHandler getHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){
        
        /*if(cdtpPackage.getCommand() == CommandEnum.connect.getCode()){
            return new LoginHandler(cdtpPackage,socketChannel);
        }
        
        if(cdtpPackage.getCommand() == CommandEnum.disconnect.getCode()){
          return new LoginHandler(cdtpPackage,socketChannel);
        }*/
        
        
        switch (CommandEnum.getByValue(cdtpPackage.getCommand())) {
          case connect://out biz            
            return new LoginHandler(socketChannel,cdtpPackage);
          case disconnect://out biz            
            return new DisconnectHandler(socketChannel, cdtpPackage);            
          default:
            return new RequestHandler(socketChannel, cdtpPackage);            
        }
    }
}
