package com.syswin.temail.gateway.handler;

import static com.syswin.temail.gateway.Constants.CDTP_VERSION;
import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Component
@Sharable
@Slf4j
public class ChannelExceptionHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("数据处理异常", cause);
    if (ctx.channel().isActive()) {
      Header header = new Header();
      CDTPServerError.Builder builder = CDTPServerError.newBuilder();
      builder.setCode(INTERNAL_ERROR.getCode());
      if (cause != null) {
        builder.setDesc(cause.getMessage());
      }
      CDTPPacket packet = new CDTPPacket(CHANNEL.getCode(), INTERNAL_ERROR.getCode(), CDTP_VERSION, header,
          builder.build().toByteArray());
      ctx.channel().writeAndFlush(packet);
    }
  }

}
