package com.syswin.temail.cdtpserver.client;

import com.syswin.temail.gateway.codec.PacketDecoder;
import com.syswin.temail.gateway.codec.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Getter
public class NettyClient {

  public static Channel startClient(String host, int port) {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .remoteAddress(new InetSocketAddress(host, port))
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) {

            socketChannel.pipeline()
                .addLast(new PacketDecoder())
                .addLast(new PacketEncoder())
                .addLast(new ClientResponseHandler());
          }
        });
    ChannelFuture future = bootstrap.connect().syncUninterruptibly();
    return future.channel();
  }

}
