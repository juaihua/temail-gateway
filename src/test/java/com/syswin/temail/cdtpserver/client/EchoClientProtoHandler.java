package com.syswin.temail.cdtpserver.client;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import com.syswin.temail.gateway.entity.CommandType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class EchoClientProtoHandler extends ChannelInboundHandlerAdapter {

  private final BlockingQueue<CDTPPacket> receivedPackages;
  private final BlockingQueue<CDTPPacket> toBeSentMessages;
  int counter = 0;
  private final Gson gson = new Gson();
  private final TemailInfo temailInfo;

  public EchoClientProtoHandler(TemailInfo temailInfo, BlockingQueue<CDTPPacket> receivedPackages, BlockingQueue<CDTPPacket> toBeSentMessages) {
    this.receivedPackages = receivedPackages;
    this.toBeSentMessages = toBeSentMessages;
    this.temailInfo = temailInfo;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    // 当被通知Channel是活跃的时候，发送一条消息
    // ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    CDTPPacket builder = new CDTPPacket();
    Header header = new Header();
    header.setPacketId("pckAgeId1234");
    header.setSignatureAlgorithm(1);

    builder.setCommand(CommandType.LOGIN.getCode());
    builder.setVersion((short) 3);

    String gsonString = gson.toJson(temailInfo);

    builder.setData(gsonString.getBytes());

    ctx.writeAndFlush(builder);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof CDTPPacket) {
      log.info("从通道接收到消息:" + msg);
      receivedPackages.offer(((CDTPPacket) msg));
    }

    log.info("############################################################");
    if (counter <= 1) {
      CDTPPacket cdtpPackage = toBeSentMessages.poll();
      if (cdtpPackage != null) {
        ctx.writeAndFlush(cdtpPackage);
        log.info("发送群聊消息...............");
        counter++;
      }
    } else {
      log.info("no send pinginfo ");
    }

    /*
     * System.out.println("############################################################");
     * if(counter <=10){ CDTPPacket cdtpPackage = null; if(counter==0){ cdtpPackage =
     * createGroupTemailBody(); ctx.writeAndFlush(cdtpPackage);
     *
     * } else if(counter==1){ cdtpPackage = sendMsgToGroupTemailBody();
     * ctx.writeAndFlush(cdtpPackage); } else{ System.out.println("YYYYYYYYYYYYYYYYY  no send"); }
     * counter++; } else{ log.info("no send pinginfo "); }
     */

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
