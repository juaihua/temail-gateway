package com.syswin.temail.gateway.client;

import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Slf4j
public class ClientResponseHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  @Setter
  private CountDownLatch latch;
  @Getter
  private CDTPPacket result;

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, CDTPPacket packet) {
    log.info("从服务器端收到的信息：{}", packet);
    result = packet;
    if (latch != null) {
      latch.countDown();
    }
  }

  public void resetLatch(CountDownLatch initLatch) {
    this.latch = initLatch;
  }
}
