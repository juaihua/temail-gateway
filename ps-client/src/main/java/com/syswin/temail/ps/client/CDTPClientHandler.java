package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.PING;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Slf4j
@Sharable
class CDTPClientHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private static final CDTPPacket HEARTBEAT_PACKET = createHeartbeatPacket();

  @Getter
  private BlockingQueue<CDTPPacket> receivedMessages = new LinkedBlockingQueue<>();

  private static CDTPPacket createHeartbeatPacket() {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(PING.getCode());
    packet.setVersion(CDTP_VERSION);
    packet.setHeader(new CDTPHeader());
    packet.setData(new byte[0]);
    return packet;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, CDTPPacket packet) {
    if (packet.isHearbeat()) {
      // 心跳响应数据不处理
      log.debug("接收到心跳数据响应");
      return;
    }
    log.debug("从服务器端收到的信息：{}", packet);
    if (packet.isInternalError()) {
      log.error(new String(packet.getData()));
    }
    receivedMessages.offer(packet);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
        log.debug("发送心跳数据");
        ctx.channel().writeAndFlush(HEARTBEAT_PACKET);
      }
    }
    super.userEventTriggered(ctx, evt);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("客户端异常！", cause);
  }
}
