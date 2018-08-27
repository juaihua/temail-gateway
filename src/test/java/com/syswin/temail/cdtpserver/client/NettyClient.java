package com.syswin.temail.cdtpserver.client;

import com.syswin.temail.gateway.codec.PacketDecoder;
import com.syswin.temail.gateway.codec.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.Getter;

import java.net.InetSocketAddress;

import static com.syswin.temail.gateway.Constants.LENGTH_FIELD_LENGTH;

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
                    .addLast("lengthFieldBasedFrameDecoder",
                            new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, LENGTH_FIELD_LENGTH, 0, 0))
                    .addLast("lengthFieldPrepender",
                            new LengthFieldPrepender(LENGTH_FIELD_LENGTH, 0, false))
                .addLast(new PacketDecoder())
                .addLast(new PacketEncoder())
                .addLast(new ClientResponseHandler());
          }
        });
    ChannelFuture future = bootstrap.connect().syncUninterruptibly();
    return future.channel();
  }

}
