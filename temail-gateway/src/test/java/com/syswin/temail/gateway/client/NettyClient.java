package com.syswin.temail.gateway.client;

import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.ps.common.codec.PacketDecoder;
import com.syswin.temail.ps.common.codec.PacketEncoder;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
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

  public final ChannelHandler responseHandler;
  private Runnable shutdownClosure;

  public NettyClient(ChannelHandler responseHandler) {
    this.responseHandler = responseHandler;
  }

  public Channel start(String host, int port) {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
        .channel(NioSocketChannel.class)
        .remoteAddress(new InetSocketAddress(host, port))
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline()
                .addLast("lengthFieldBasedFrameDecoder",
                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, LENGTH_FIELD_LENGTH, 0, 0))
                .addLast("lengthFieldPrepender",
                    new LengthFieldPrepender(LENGTH_FIELD_LENGTH, 0, false))
                .addLast(new PacketDecoder(new SimpleBodyExtractor()))
                .addLast(new PacketEncoder())
                .addLast(new ClientExceptionHandler())
                .addLast(responseHandler);
          }
        });
    ChannelFuture future = bootstrap.connect().syncUninterruptibly();
    shutdownClosure = group::shutdownGracefully;
    return future.channel();
  }

  void stop() {
    if (shutdownClosure != null) {
      shutdownClosure.run();
    }
  }
}
