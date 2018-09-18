package com.syswin.temail.ps.client;


import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.ps.client.utils.StringUtil;
import com.syswin.temail.ps.common.codec.PacketDecoder;
import com.syswin.temail.ps.common.codec.PacketEncoder;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
@Slf4j
class CDTPClient {

  static final int DEFAULT_EXECUTE_TIMEOUT = 300;

  private final String host;
  private final int port;
  private final ConcurrentHashMap<String, Request> requestMap = new ConcurrentHashMap<>();
  private final int writeIdleTimeSeconds;
  private final int maxRetryInternal;
  private final EventLoopGroup bossGroup = new NioEventLoopGroup();

  private CDTPClientHandler CDTPClientHandler;
  private Channel channel;

  CDTPClient(String host, int port, int writeIdleTimeSeconds, int maxRetryInternal) {
    this.host = host;
    this.port = port;
    this.writeIdleTimeSeconds = writeIdleTimeSeconds;
    this.maxRetryInternal = maxRetryInternal;
    CDTPClientHandler = new CDTPClientHandler();
  }

  public void connect() {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(bossGroup)
        .channel(NioSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO));

    final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap) {

      public ChannelHandler[] handlers() {
        return new ChannelHandler[]{
            this,
            new IdleStateHandler(0, writeIdleTimeSeconds, 0),
            new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, LENGTH_FIELD_LENGTH),
            new LengthFieldPrepender(LENGTH_FIELD_LENGTH),
            new PacketDecoder(),
            new PacketEncoder(),
            CDTPClientHandler,
        };
      }
    };
    realConnect(bootstrap, watchdog.handlers());
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
      Response response = new Response();
      Request request = new Request(reqPacket, packet -> {
        response.respPacket = packet;
        latch.countDown();
      });
      requestMap.put(packetId, request);
      channel.writeAndFlush(reqPacket);
      latch.await(timeout, timeUnit);
      return response.respPacket;
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
    Request request = new Request(reqPacket, responseConsumer, errorConsumer, timeout, timeUnit);
    ScheduledFuture<?> timeoutFuture = bossGroup.schedule(new TimeoutTask(request), timeout, timeUnit);
    request.setTimeoutFuture(timeoutFuture);
    requestMap.put(packetId, request);
    channel.writeAndFlush(reqPacket);
  }

  private ChannelFuture realConnect(Bootstrap bootstrap, ChannelHandler[] handlers) {
    //进行连接
    ChannelFuture future;
    bootstrap.handler(new ChannelInitializer<Channel>() {
      //初始化channel
      @Override
      protected void initChannel(Channel ch) {
        ch.pipeline().addLast(handlers);
      }
    });
    try {
      future = bootstrap.connect(host, port).sync();
      channel = future.channel();
      return future;
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
              request = requestMap.values().iterator().next();
            } else {
              log.error("服务器返回无主错误，而客户端有多个请求，无法处理, 返回值：{}", respPacket);
            }
          }
          if (request != null) {
            ScheduledFuture<?> timeoutFuture = request.getTimeoutFuture();
            if (timeoutFuture != null) {
              timeoutFuture.cancel(false);
            }
            Consumer<CDTPPacket> responseConsumer = request.getResponseConsumer();
            responseConsumer.accept(respPacket);
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
      Consumer<Throwable> errorConsumer = request.getErrorConsumer();
      if (errorConsumer != null) {
        errorConsumer.accept(new TimeoutException());
      }
      requestMap.remove(request.getReqPacket().getHeader().getPacketId());
    }
  }

  @Sharable
  public abstract class ConnectionWatchdog
      extends ChannelInboundHandlerAdapter
      implements TimerTask {

    private final Timer timer = new HashedWheelTimer();
    private Bootstrap bootstrap;
    private int attempts;

    protected ConnectionWatchdog(Bootstrap bootstrap) {
      this.bootstrap = bootstrap;
    }

    /**
     * channel链路每次active的时候，将其连接的次数重新☞ 0
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
      log.debug("当前链路已经激活了，重连尝试次数重新置为0");
      attempts = 0;
      ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      log.debug("链接关闭，将进行重连");

      int timeout = 1 << attempts;
      if (timeout > maxRetryInternal) {
        timeout = maxRetryInternal;
      } else {
        attempts++;
      }

      //重连的间隔时间会越来越长
      timer.newTimeout(this, timeout, TimeUnit.SECONDS);
      ctx.fireChannelInactive();
    }

    @Override
    public void run(Timeout timeout) {
      ChannelFuture future = realConnect(bootstrap, handlers());
      //future对象
      future.addListener((ChannelFutureListener) f -> {
        //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
        if (!f.isSuccess()) {
          log.debug("重连失败");
          f.channel().pipeline().fireChannelInactive();
        } else {
          log.debug("重连成功");
        }
      });
    }

    abstract ChannelHandler[] handlers();
  }

}
