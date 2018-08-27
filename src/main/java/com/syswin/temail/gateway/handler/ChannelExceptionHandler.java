package com.syswin.temail.gateway.handler;

import static com.syswin.temail.gateway.Constants.CDTP_VERSION;
import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Component
public class ChannelExceptionHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    if (ctx.channel().isActive()) {
      Header header = new Header();
      byte[] errorData = cause.getMessage().getBytes(StandardCharsets.UTF_8);
      CDTPPacket packet = new CDTPPacket(CHANNEL.getCode(), INTERNAL_ERROR.getCode(), CDTP_VERSION, header, errorData);
      ctx.channel().writeAndFlush(packet);
    }
  }

}
