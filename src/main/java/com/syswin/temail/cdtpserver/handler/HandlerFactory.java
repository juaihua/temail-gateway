package com.syswin.temail.cdtpserver.handler;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by weis on 18/8/8.
 */
public class HandlerFactory {

    public static BaseHandler getHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){
        if(cdtpPackage.getCommand() == 1){
            return new LoginHandler(cdtpPackage,socketChannel);
        }
        return null;
    }
}
