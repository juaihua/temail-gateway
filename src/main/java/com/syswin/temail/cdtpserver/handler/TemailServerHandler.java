package com.syswin.temail.cdtpserver.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syswin.temail.cdtpserver.connection.ActiveTemailManager;
import com.syswin.temail.cdtpserver.constants.ConstantsAttributeKey;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.CommandEnum;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketOptEnum;
import com.syswin.temail.cdtpserver.handler.base.BaseHandler;
import com.syswin.temail.cdtpserver.handler.factory.HandlerFactory;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.status.TemailSocketSyncClient;
import com.syswin.temail.cdtpserver.utils.TemailSocketBuilderUtil;

/**
 * Created by weis on 18/8/2.
 */
@ChannelHandler.Sharable
public class TemailServerHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());
  // heartbeat lose counter
  private int counter;

  private BaseHandler handler;

  @Setter
  @Getter
  TemailSocketSyncClient temailSocketSyncClient;

  @Setter
  @Getter
  private HandlerFactory handlerFactory;

  @Setter
  @Getter
  private TemailMqInfo temailMqInfo;

  @Setter
  @Getter
  private TemailServerProperties temailServerProperties;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {

    if (msg instanceof CDTPPackage) {
      CDTPPackage cdtpPackage = (CDTPPackage) msg;
      LOGGER.info("接收到客户服发送过来的ProtoBuf CDTPPackage 信息:{} ", cdtpPackage.toString());
      if (cdtpPackage.getCommand() == CommandEnum.ping.getCode()
          || cdtpPackage.getCommand() == CommandEnum.pong.getCode()) {
        // 如果是心跳包
        handleHeartbreat(ctx, cdtpPackage);
      } else {
        handleData(ctx, cdtpPackage);
      }
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("client is active,  remoteAddress {}", ctx.channel().remoteAddress().toString());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    LOGGER.info("socketChannel {} client is inactive, 当前连接数:{}", ctx.channel().remoteAddress()
        .toString(), ActiveTemailManager.getOnlineTemailMap().size());
    if (null != ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get()) {

      // 有一种情况心跳丢失,在userEventTriggered 中已经把Temail信息从ActiveTemailManager移除, 此时Temail为空
      TemailInfo temailInfo =
          ActiveTemailManager.getOne(ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get());
      if (null != temailInfo) {

        LOGGER.info("socketChannel{} , TemailKey {} 丢失 {} 次心跳, 强制断开连接 , 并且从状态管理中移除",
            ((SocketChannel) ctx.channel()).remoteAddress().toString(),
            ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get(), counter);
        TemailSocketInfo temailSocketInfo =
            TemailSocketBuilderUtil.temailSocketBuilder(temailInfo, temailMqInfo,
                TemailSocketOptEnum.del.toString());
        temailSocketSyncClient.updateTemailSocketInfToRemote(temailSocketInfo);
        ActiveTemailManager.removeByTemailKey(ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY)
            .get());
      }
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    LOGGER.info("在 TemailServerHandler 中  socketChannel {} 通道中接收的消息读取完毕.", ctx.channel()
        .remoteAddress());
  }


  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    LOGGER.info("TEMAIL_KEY:" + ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get());
    if (evt instanceof IdleStateEvent) {// 指定时间内,通道没有任何数据
      if (counter >= temailServerProperties.getAllow_lose_count()) {

        TemailInfo temailInfo =
            ActiveTemailManager.getOne(ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get());
        if (null != temailInfo) {
          TemailSocketInfo temailSocketInfo =
              TemailSocketBuilderUtil.temailSocketBuilder(temailInfo, temailMqInfo,
                  TemailSocketOptEnum.del.toString());
          temailSocketSyncClient.updateTemailSocketInfToRemote(temailSocketInfo);
          ActiveTemailManager.removeByTemailKey(ctx.channel()
              .attr(ConstantsAttributeKey.TEMAIL_KEY).get());
          LOGGER.info(
              "socketChannel{} , TemailKey is {} 空闲超时 , 丢失 {} 次心跳, 已与Client断开连接 , 并且从状态管理中移除",
              ((SocketChannel) ctx.channel()).remoteAddress().toString(),
              ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get(), counter);
        }

        ctx.channel().close().sync();
        // 清理状态数据
      } else {
        counter++;
        SendMsg.sendHeartbeat((SocketChannel) ctx.channel(), CommandEnum.ping);
        LOGGER.info("socketChannel {} , TemailKey  {} lose : {} heartbeat packet",
            ((SocketChannel) ctx.channel()).remoteAddress().toString(),
            ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get(), counter);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }


  /**
   * 处理ping心跳包
   *
   * @param ctx 上下文
   * @param packet 包内容
   */
  private void handleHeartbreat(ChannelHandlerContext ctx, CDTPPackage packet) {

    counter = 0;

    if (packet.getCommand() == CommandEnum.ping.getCode()) {
      SendMsg.sendHeartbeat((SocketChannel) ctx.channel(), CommandEnum.pong);
      LOGGER.info("server send heartbeat ping packet to:"
          + ctx.channel().remoteAddress().toString());
    }

    if (packet.getCommand() == CommandEnum.pong.getCode()) {
      LOGGER.info("server received heartbeat pong packet from:"
          + ctx.channel().remoteAddress().toString());
    }

  }

  /**
   * 处理业务数据包
   *
   * @param ctx 上下文
   * @param cdtpPackage 包内容
   */
  private void handleData(ChannelHandlerContext ctx, CDTPPackage cdtpPackage) {
    counter = 0;
    // 登陆单独处理
    if (cdtpPackage.getCommand() == CommandEnum.connect.getCode()) {
      handler = handlerFactory.getHandler(cdtpPackage, (SocketChannel) ctx.channel());
      handler.process();
    } else {
      if (null != ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get()) {
        // 业务处理
        handler = handlerFactory.getHandler(cdtpPackage, (SocketChannel) ctx.channel());
        handler.process();
      } else {
        LOGGER.info("尚未登录成功, 非法的连接************************");
        ctx.channel().close();
      }
    }
  }

}
