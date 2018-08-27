package com.syswin.temail.gateway.connection;

import static com.syswin.temail.gateway.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.codec.PacketDecoder;
import com.syswin.temail.gateway.codec.PacketEncoder;
import com.syswin.temail.gateway.handler.ChannelExceptionHandler;
import com.syswin.temail.gateway.handler.IdleHandler;
import com.syswin.temail.gateway.handler.TemailGatewayHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by weis on 18/8/2.
 */
@Slf4j
@Order(1)
@Component
public class TemailGatewayServer implements ApplicationRunner {

  @Resource
  private TemailGatewayProperties properties;
  @Resource
  private IdleStateHandler idleStateHandler;
  @Resource
  private IdleHandler idleHandler;
  @Resource
  private PacketDecoder packetDecoder;
  @Resource
  private PacketEncoder packetEncoder;
  @Resource
  private TemailGatewayHandler temailGatewayHandler;
  @Resource
  private ChannelExceptionHandler channelExceptionHandler;

  @Override
  public void run(ApplicationArguments args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();// 默认 cup

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap.group(bossGroup, workerGroup)
        // 使用指定端口设置套接字地址
        .channel(NioServerSocketChannel.class)
        // 指定使用NIO传输Channel
        .localAddress(new InetSocketAddress(properties.getPort()))
        // 通过NoDelay禁用Nagle,使消息立即发送出去
        .childOption(ChannelOption.TCP_NODELAY, true)
        // 保持长连接状态
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel channel) {
            channel.pipeline()
                .addLast("idleStateHandler", idleStateHandler)
                .addLast("idleHandler", idleHandler)
                .addLast("lengthFieldBasedFrameDecoder",
                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, LENGTH_FIELD_LENGTH, 0, 0))
                .addLast("lengthFieldPrepender",
                    new LengthFieldPrepender(LENGTH_FIELD_LENGTH, 0, false))
                .addLast("packetDecoder", packetDecoder)
                .addLast("packetEncoder", packetEncoder)
                .addLast("temailGatewayHandler", temailGatewayHandler)
                .addLast("channelExceptionHandler", channelExceptionHandler);
          }
        });

    // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
    bootstrap.bind().syncUninterruptibly();
    log.info("Temail 服务器已启动,正在监听用户的请求......");
  }
}
