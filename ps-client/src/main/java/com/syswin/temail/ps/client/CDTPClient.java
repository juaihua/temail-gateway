package com.syswin.temail.ps.client;


import static com.syswin.temail.ps.client.Constants.DEFAULT_EXECUTE_TIMEOUT;
import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.ps.client.utils.StringUtil;
import com.syswin.temail.ps.common.codec.PacketDecoder;
import com.syswin.temail.ps.common.codec.PacketEncoder;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
@Slf4j
class CDTPClient {

  private final String host;
  private final int port;
  private final ConcurrentHashMap<String, Request> requestMap = new ConcurrentHashMap<>();
  private final int idleTimeSeconds;
  private final EventLoopGroup bossGroup = new NioEventLoopGroup();

  private Bootstrap bootstrap;
  private Channel channel;
  private CDTPClientHandler CDTPClientHandler;

  CDTPClient(String host, int port, int idleTimeSeconds) {
    this.host = host;
    this.port = port;
    this.idleTimeSeconds = idleTimeSeconds;
    CDTPClientHandler = new CDTPClientHandler();
  }

  public void connect() {
    bootstrap = new Bootstrap();
    bootstrap.group(bossGroup)
        .channel(NioSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO));

    realConnect();
    bossGroup.submit(this::handleResponse);

    Runtime.getRuntime().addShutdownHook(new Thread(bossGroup::shutdownGracefully));
  }

  public CDTPPacket syncExecute(CDTPPacket reqPacket) {
    return syncExecute(reqPacket, DEFAULT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
  }

  public CDTPPacket syncExecute(CDTPPacket reqPacket, long timeout, TimeUnit timeUnit) {
    try {
      String packetId = reqPacket.getHeader()
          .getPacketId();
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference<CDTPPacket> respPpacket = new AtomicReference<>();
      AtomicReference<PsClientException> exception = new AtomicReference<>();
      Request request = new Request(reqPacket,
          respPacket -> {
            respPpacket.set(respPacket);
            latch.countDown();
          },
          throwable -> {
            if (throwable instanceof PsClientException) {
              exception.set((PsClientException) throwable);
            } else {
              exception.set(new PsClientException("PsClient请求失败，请求参数" + reqPacket, throwable));
            }
            latch.countDown();
          });
      requestMap.put(packetId, request);
      channel.writeAndFlush(reqPacket);
      latch.await(timeout, timeUnit);
      if (exception.get() != null) {
        throw exception.get();
      } else {
        return respPpacket.get();
      }
    } catch (InterruptedException e) {
      throw new PsClientException("执行CDTP请求出错", e);
    }
  }

  public void asyncExecute(CDTPPacket reqPacket, Consumer<CDTPPacket> responseConsumer,
      Consumer<Throwable> errorConsumer) {
    asyncExecute(reqPacket, responseConsumer, errorConsumer, DEFAULT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
  }

  public void asyncExecute(CDTPPacket reqPacket, Consumer<CDTPPacket> responseConsumer,
      Consumer<Throwable> errorConsumer, long timeout, TimeUnit timeUnit) {
    String packetId = reqPacket.getHeader()
        .getPacketId();
    Request request = new Request(reqPacket, responseConsumer, errorConsumer);
    ScheduledFuture<?> timeoutFuture = bossGroup.schedule(new TimeoutTask(request), timeout, timeUnit);
    request.setTimeoutFuture(timeoutFuture);
    requestMap.put(packetId, request);
    channel.writeAndFlush(reqPacket);
  }

  boolean isActive() {
    return channel != null && channel.isActive();
  }

  private ChannelHandler[] handlers() {
    return new ChannelHandler[]{
//        watchdog,
        new IdleStateHandler(0, idleTimeSeconds, 0),
        new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, LENGTH_FIELD_LENGTH),
        new LengthFieldPrepender(LENGTH_FIELD_LENGTH),
        new PacketDecoder(),
        new PacketEncoder(),
        CDTPClientHandler,
    };
  }

  void realConnect() {
    //进行连接
    ChannelFuture future;
    bootstrap.handler(new ChannelInitializer<Channel>() {
      //初始化channel
      @Override
      protected void initChannel(Channel ch) {
        ch.pipeline().addLast(handlers());
      }
    });
    try {
      future = bootstrap.connect(host, port).sync();
      channel = future.channel();
    } catch (InterruptedException e) {
      throw new PsClientException("连接服务器出错！", e);
    }
  }


  private void handleResponse() {
    do {
      try {
        CDTPPacket respPacket = CDTPClientHandler.getReceivedMessages().take();
        try {

          CDTPHeader header = respPacket.getHeader();
          String packetId = header.getPacketId();
          Request request = null;
          if (StringUtil.hasText(packetId)) {
            request = requestMap.remove(packetId);
          } else {
            // 无主的服务器端错误，而客户端只有一个请求，那就是它了
            if (requestMap.size() == 1) {
              Request tempRequest = requestMap.values().iterator().next();
              String reqPacketId = tempRequest.getReqPacket().getHeader().getPacketId();
              if (requestMap.size() == 1) {
                request = requestMap.remove(reqPacketId);
              }
            } else {
              log.error("服务器返回无主错误，而客户端有多个请求，无法处理, 返回值：{}", respPacket);
            }
          }
          if (request != null) {
            ScheduledFuture<?> timeoutFuture = request.getTimeoutFuture();
            if (timeoutFuture != null) {
              timeoutFuture.cancel(false);
            }
            request.getResponseConsumer().accept(respPacket);
          }
        } catch (RuntimeException e) {
          // 其他异常无须处理
          log.error("处理请求响应时出错，响应对象：" + respPacket, e);
        }
      } catch (InterruptedException e) {
        log.error("响应处理进程被中止，无法处理服务器返回的消息！", e);
        Thread.currentThread().interrupt();
      }
    } while (!Thread.interrupted());
  }

  private final class TimeoutTask implements Runnable {

    private Request request;

    private TimeoutTask(Request request) {
      this.request = request;
    }

    @Override
    public void run() {
      log.error("请求超时！请求对象：{}", request);
      Consumer<Throwable> errorConsumer = request.getErrorConsumer();
      if (errorConsumer != null) {
        errorConsumer.accept(new TimeoutException("请求超时,请求对象" + request));
      }
      requestMap.remove(request.getReqPacket().getHeader().getPacketId());
    }
  }
}
