package com.syswin.temail.gateway.handler;

import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.LOGIN;
import static com.syswin.temail.gateway.entity.CommandType.LOGOUT;
import static com.syswin.temail.gateway.entity.CommandType.PING;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.service.HeartBeatService;
import com.syswin.temail.gateway.service.RequestService;
import com.syswin.temail.gateway.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by weis on 18/8/2.
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class TemailGatewayHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private SessionService sessionService;
  private RequestService requestService;
  private HeartBeatService heartBeatService;

  public TemailGatewayHandler(SessionService sessionService,
      RequestService requestService, HeartBeatService heartBeatService) {
    this.sessionService = sessionService;
    this.requestService = requestService;
    this.heartBeatService = heartBeatService;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, CDTPPacket packet) {
    Channel channel = ctx.channel();
    if (packet.getCommandSpace() == CHANNEL.getCode()) {
      if (packet.getCommand() == PING.getCode()) {
        heartBeatService.pong(channel, packet);
      } else if (packet.getCommand() == LOGIN.getCode()) {
        sessionService.login(channel, packet);
      } else if (packet.getCommand() == LOGOUT.getCode()) {
        sessionService.logout(channel, packet);
      } else {
        // 其他连接请求
      }
    } else {
      requestService.handleRequest(channel, packet);
    }
  }
}
