package com.syswin.temail.gateway.handler;

import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.LOGIN;
import static com.syswin.temail.gateway.entity.CommandType.LOGOUT;
import static com.syswin.temail.gateway.entity.CommandType.PING;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.exception.PacketException;
import com.syswin.temail.gateway.service.HeartBeatService;
import com.syswin.temail.gateway.service.RequestHandler;
import com.syswin.temail.gateway.service.SessionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class TemailGatewayHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private final SessionHandler sessionService;
  private final RequestHandler requestService;
  private final HeartBeatService heartBeatService;

  public TemailGatewayHandler(
      SessionHandler sessionService,
      RequestHandler requestService,
      HeartBeatService heartBeatService) {
    this.sessionService = sessionService;
    this.requestService = requestService;
    this.heartBeatService = heartBeatService;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, CDTPPacket packet) {
    try {
      Channel channel = ctx.channel();
      if (packet.getCommandSpace() == CHANNEL.getCode()) {
        if (packet.getCommand() == PING.getCode()) {
          heartBeatService.pong(channel, packet);
        } else if (packet.getCommand() == LOGIN.getCode()) {
          sessionService.login(channel, packet);
        } else if (packet.getCommand() == LOGOUT.getCode()) {
          // TODO: 2018/8/31 only allowed after login
          sessionService.logout(channel, packet);
        } else {
          log.warn("Received unknown command {} {}", Integer.toHexString(packet.getCommandSpace()), Integer.toHexString(packet.getCommand()));
        }
      } else {
        requestService.handleRequest(channel, packet);
      }
    } catch (Exception e) {
      throw new PacketException(e, packet);
    }
  }
}
