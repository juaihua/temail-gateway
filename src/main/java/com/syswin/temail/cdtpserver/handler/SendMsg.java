package com.syswin.temail.cdtpserver.handler;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.CommandEnum;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by weis on 18/8/9.
 */
@Slf4j
public class SendMsg {

  /**
   * 发送心跳命令
   *
   * @param command 心跳类型   ping   pong
   */
  public static void sendHeartbeat(SocketChannel socketChannel, CommandEnum command) {
    CDTPPackageProto.CDTPPackage.Builder builder = CDTPPackageProto.CDTPPackage.newBuilder();
    builder.setCommand(command.getCode());//设置心跳命令

    CDTPPackageProto.CDTPPackage cdtpPackage = builder.build();
    log.info("发送心跳指令信息: {}", cdtpPackage.toString());
    socketChannel.writeAndFlush(cdtpPackage);
  }


  //public static void sendToTemail(String toTemail,CDTPPackageProto cdtpPackageProto,SocketChannel socketChannel){
  public static void sendToTemail(CDTPPackage ctPackage, SocketChannel socketChannel) {
    log.info("从MQ中提取信息, 向对应的通道中发送:{}", ctPackage.toString());
    socketChannel.writeAndFlush(ctPackage);
  }


}
