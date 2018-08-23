package com.syswin.temail.cdtpserver.client;

import com.syswin.temail.cdtpserver.codec.PacketDecoder;
import com.syswin.temail.cdtpserver.codec.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by weis on 18/8/3.
 */
public class EchoClient {

  //private final static String HOST = "192.168.1.194";
  //private final static String HOST = "192.168.15.9";
  private final static String HOST = "127.0.0.1";
  private final static int PORT = 8099;

  public static void start() {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
        .remoteAddress(new InetSocketAddress(HOST, PORT))
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) {

            socketChannel.pipeline().addLast(new PacketDecoder());
            socketChannel.pipeline().addLast(new PacketEncoder());

            socketChannel.pipeline().addLast(new EchoClientProtoHandler());
          }
        });
    try {
      // ChannelFuture f = bootstrap.connect().sync();
      // f.channel().closeFuture().sync();
      Channel channel = bootstrap.connect().sync().channel();
      // 发送json字符串
      // String msg =
      // "{\"command\":110,\"from\":\"weisheng@temail.com\",\"to\":\"gaojianhui@temail.com\",\"version\":\"1.0.0\"}\n";
      // // String jsonString = "{name:'Antony',age:'22',sex:'male',telephone:'88888'}";
      // channel.writeAndFlush(msg);
      channel.closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      group.shutdownGracefully();
    }
  }

  public static void main(String[] args) {
    start();
  }

}
