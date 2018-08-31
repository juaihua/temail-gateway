package com.syswin.temail.gateway.client;

import static com.syswin.temail.gateway.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.gateway.codec.PacketDecoder;
import com.syswin.temail.gateway.codec.PacketEncoder;
import com.syswin.temail.gateway.codec.SimpleBodyExtractor;
import com.syswin.temail.gateway.entity.CDTPPacket;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
public class YHCNettyClient {

  private YHCClientResponseHandler responseHandler;
  private String host;
  private int port;
  private Channel channel;

  public YHCNettyClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void start() {
    responseHandler = new YHCClientResponseHandler();

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
                .addLast(responseHandler);
          }
        });
    ChannelFuture future = bootstrap.connect().syncUninterruptibly();
    channel = future.channel();
    Runtime.getRuntime().addShutdownHook(new Thread(group::shutdownGracefully));
  }

  public CDTPPacket syncExecute(CDTPPacket reqPacket) {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      responseHandler.resetLatch(latch);
      channel.writeAndFlush(reqPacket);
      latch.await();
      return responseHandler.getResult();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public CDTPPacket getNewResult() {
    Awaitility.await().atMost(300, TimeUnit.SECONDS).until(() -> responseHandler.isNewResult());
    return responseHandler.getResult();
  }

}
