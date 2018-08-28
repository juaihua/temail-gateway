package com.syswin.temail.gateway.client;

import static com.syswin.temail.gateway.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.gateway.codec.PacketDecoder;
import com.syswin.temail.gateway.codec.PacketEncoder;
import com.syswin.temail.gateway.handler.ChannelExceptionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import java.net.InetSocketAddress;
import lombok.Getter;

@Getter
public class NettyClient {

  public static ClientResponseHandler responseHandler = new ClientResponseHandler();

  public static Channel startClient(String host, int port) {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
        .channel(NioSocketChannel.class)
//        .option(ChannelOption.SO_BACKLOG, 1024)
        .remoteAddress(new InetSocketAddress(host, port))
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline()
                .addLast("lengthFieldBasedFrameDecoder",
                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, LENGTH_FIELD_LENGTH, 0, 0))
                .addLast("lengthFieldPrepender",
                    new LengthFieldPrepender(LENGTH_FIELD_LENGTH, 0, false))
                .addLast(new PacketDecoder())
                .addLast(new PacketEncoder())
                .addLast(new ChannelExceptionHandler())
                .addLast(responseHandler);
          }
        });
    ChannelFuture future = bootstrap.connect().syncUninterruptibly();
//    Runtime.getRuntime().addShutdownHook(new Thread(group::shutdownGracefully));
    return future.channel();
  }

}
