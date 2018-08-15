package com.syswin.temail.cdtpserver.handler;

import javax.annotation.Resource;

import io.netty.channel.socket.SocketChannel;

import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.config.TemailServerConfig;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.utils.CommandEnum;

/**
 * Created by weis on 18/8/8.
 */
@Component
public class HandlerFactory {

    @Resource 
    TemailServerConfig   temailServerConfig;
    
    //public static BaseHandler getHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){
    public BaseHandler getHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){    
      /*switch (CommandEnum.getByValue(cdtpPackage.getCommand())) {
      case connect://out biz            
        return new LoginHandler(socketChannel,cdtpPackage);
      case disconnect://out biz            
        return new DisconnectHandler(socketChannel, cdtpPackage);            
      default:
        return new RequestHandler(socketChannel, cdtpPackage);            
    }*/
      
        if(cdtpPackage.getCommand() == CommandEnum.connect.getCode()){
            return new LoginHandler(socketChannel,cdtpPackage, temailServerConfig);
        }
        else if(cdtpPackage.getCommand() == CommandEnum.disconnect.getCode()){
          return new DisconnectHandler(socketChannel,cdtpPackage, temailServerConfig);
        }
        else{
            return new RequestHandler(socketChannel, cdtpPackage, temailServerConfig); 
        }       
    }
}
