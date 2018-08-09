package com.syswin.temail.cdtpserver.handler;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import io.netty.channel.socket.SocketChannel;
import lombok.Data;

/**
 * Created by weis on 18/8/8.
 */
@Data
public abstract class BaseHandler {

    private SocketChannel socketChannel;

    private CDTPPackageProto.CDTPPackage cdtpPackage;

    public BaseHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage){

        this.socketChannel = socketChannel;
        this.cdtpPackage = cdtpPackage;

    }


    public abstract void process();
}
