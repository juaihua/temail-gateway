package com.syswin.temail.cdtpserver.handler;

import lombok.extern.slf4j.Slf4j;
import io.netty.channel.socket.SocketChannel;

import com.syswin.temail.cdtpserver.TemailServerProperties;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;

/**
 * Created by weis on 18/8/8.
 */
@Slf4j
public class HeartbeatHandler extends BaseHandler {

    public HeartbeatHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){
      super(socketChannel,cdtpPackage);
    }
  
    public HeartbeatHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel, TemailServerProperties temailServerConfig){
        super(socketChannel,cdtpPackage, temailServerConfig);
    }

    @Override
    public void process() {

        CDTPPackageProto.CDTPPackage.Builder builder= CDTPPackageProto.CDTPPackage.newBuilder();
//        builder.setAlgorithm(1);
        builder.setCommand(2);//设置心跳应答command
//        builder.setVersion(3);

        CDTPPackageProto.CDTPPackage ctPackage = builder.build();
        log.info("发送心跳信息:{}", ctPackage.toString());
        this.getSocketChannel().writeAndFlush(ctPackage);
    }
}
