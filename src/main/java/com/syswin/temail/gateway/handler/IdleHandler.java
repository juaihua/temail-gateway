package com.syswin.temail.gateway.handler;

import com.syswin.temail.gateway.service.SessionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleUserEventChannelHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Slf4j
@Component
public class IdleHandler extends SimpleUserEventChannelHandler<IdleStateEvent> {

  private SessionService sessionService;

  public IdleHandler(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  protected void eventReceived(ChannelHandlerContext ctx, IdleStateEvent evt) {
    sessionService.terminateChannel(ctx.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    sessionService.terminateChannel(ctx.channel());
  }

}
