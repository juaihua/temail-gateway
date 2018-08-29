package com.syswin.temail.gateway.handler;

import static com.syswin.temail.gateway.Constants.CDTP_VERSION;
import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.entity.CDTPHeader;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.gateway.exception.PacketException;
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
      CDTPPacket packet;
      if (cause instanceof PacketException) {
        PacketException packetException = (PacketException) cause;
        packet = packetException.getPacket();
      } else {
        CDTPHeader header = new CDTPHeader();
        packet = new CDTPPacket();
        packet.setHeader(header);
        packet.setVersion(CDTP_VERSION);
      }
      packet.setCommandSpace(CHANNEL.getCode());
      packet.setCommand(INTERNAL_ERROR.getCode());
      CDTPServerError.Builder builder = CDTPServerError.newBuilder();
      builder.setCode(INTERNAL_ERROR.getCode());
      if (cause != null) {
        builder.setDesc(cause.getMessage());
      }
      packet.setData(builder.build().toByteArray());
      ctx.channel().writeAndFlush(packet);
    }
  }

}
