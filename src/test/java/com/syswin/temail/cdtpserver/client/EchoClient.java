package com.syswin.temail.cdtpserver.client;

import com.syswin.temail.cdtpserver.codec.PacketDecoder;
import com.syswin.temail.cdtpserver.codec.PacketEncoder;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by weis on 18/8/3.
 */
public class EchoClient {

  private final BlockingQueue<CDTPPackage> receivedPackages;
  private final BlockingQueue<CDTPPackage> toBeSentPackages;
  private final TemailInfo temailInfo;
  private Runnable runnable;

  EchoClient(String temail,
      String devId,
      BlockingQueue<CDTPPackage> toBeSentPackages,
      BlockingQueue<CDTPPackage> receivedPackages) {
    this.receivedPackages = receivedPackages;
    this.toBeSentPackages = toBeSentPackages;
    temailInfo = new TemailInfo();
    temailInfo.setTemail(temail);
    // temailInfo.setTemail("sean@t.email");
    temailInfo.setDevId(devId);
  }

  public static void main(String[] args) {
    new EchoClient("jack@t.email", "devId", new LinkedBlockingQueue<>(), new LinkedBlockingQueue<>())
        .start("192.168.1.194", 8099);
  }

  void start(String host, int port) {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
        .remoteAddress(new InetSocketAddress(host, port))
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) {

            socketChannel.pipeline().addLast(new PacketDecoder());
            socketChannel.pipeline().addLast(new PacketEncoder());

            socketChannel.pipeline().addLast(new EchoClientProtoHandler(temailInfo, receivedPackages, toBeSentPackages));
          }
        });
    try {
      bootstrap.connect().sync();
      runnable = group::shutdownGracefully;
      Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  void stop() {
    runnable.run();
  }
}
