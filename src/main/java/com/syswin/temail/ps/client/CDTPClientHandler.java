package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;
import static com.syswin.temail.ps.common.entity.CommandType.PING;
import static com.syswin.temail.ps.common.entity.CommandType.PONG;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
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
    packet.setCommandSpace(CHANNEL.getCode());
    packet.setCommand(PING.getCode());
    packet.setVersion(CDTP_VERSION);
    return packet;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, CDTPPacket packet) {
    if (packet.getCommandSpace() == CHANNEL.getCode()) {
      if (packet.getCommand() == PONG.getCode()) {
        // 心跳响应数据不处理
        log.debug("接收到心跳数据响应");
        return;
      } else if (packet.getCommand() == INTERNAL_ERROR.getCode()) {
        // 服务器端异常，直接记录错误日志
        Object errorData;
        try {
          errorData = CDTPServerError.parseFrom(packet.getData());
        } catch (InvalidProtocolBufferException e) {
          errorData = packet;
        }
        log.error("服务器返回错误的信息，异常数据为：{}", errorData);
        return;
      }
    }
    log.info("从服务器端收到的信息：{}", packet);
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


}
