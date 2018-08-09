package com.syswin.temail.cdtpserver.handler;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by weis on 18/8/8.
 */
public class HeartbeatHandler extends BaseHandler {

    public HeartbeatHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){
        super(socketChannel,cdtpPackage);
    }

    @Override
    public void process() {

        CDTPPackageProto.CDTPPackage.Builder builder= CDTPPackageProto.CDTPPackage.newBuilder();
//        builder.setAlgorithm(1);
        builder.setCommand(2);//设置心跳应答command
//        builder.setVersion(3);

        CDTPPackageProto.CDTPPackage ctPackage = builder.build();

        this.getSocketChannel().writeAndFlush(ctPackage);
    }
}
