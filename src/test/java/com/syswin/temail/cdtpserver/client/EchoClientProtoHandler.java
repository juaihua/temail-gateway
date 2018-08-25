package com.syswin.temail.cdtpserver.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.CDTPBody;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.CommandEnum;

/**
 * Created by weis on 18/8/3.
 */
@Slf4j
@ChannelHandler.Sharable
public class EchoClientProtoHandler extends ChannelInboundHandlerAdapter {

  private final BlockingQueue<CDTPPackage> receivedPackages;
  private final BlockingQueue<CDTPPackage> toBeSentMessages;
  int counter = 0;
  private final Gson gson = new Gson();
  private final TemailInfo temailInfo;

  public EchoClientProtoHandler(TemailInfo temailInfo, BlockingQueue<CDTPPackage> receivedPackages, BlockingQueue<CDTPPackage> toBeSentMessages) {
    this.receivedPackages = receivedPackages;
    this.toBeSentMessages = toBeSentMessages;
    this.temailInfo = temailInfo;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    // 当被通知Channel是活跃的时候，发送一条消息
    // ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    CDTPPackage.Builder builder = CDTPPackage.newBuilder();
    builder.setAlgorithm(1);
    builder.setCommand(CommandEnum.connect.getCode());
    builder.setPkgId("pckAgeId1234");
    builder.setVersion(3);

    String gsonString = gson.toJson(temailInfo);

    builder.setData(ByteString.copyFrom(gsonString, Charset.defaultCharset()));
    CDTPPackage ctPackage = builder.build();

    ctx.writeAndFlush(ctPackage);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof CDTPPackage) {
      log.info("从通道接收到消息:" + msg);
      receivedPackages.offer(((CDTPPackage) msg));
    }

    log.info("############################################################");
    if (counter <= 1) {
      CDTPPackage cdtpPackage = toBeSentMessages.poll();
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
     * if(counter <=10){ CDTPPackage cdtpPackage = null; if(counter==0){ cdtpPackage =
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


  // 申请群注册
  private CDTPPackage createGroupTemailBody() {

    CDTPPackage.Builder builder = CDTPPackage.newBuilder();
    builder.setAlgorithm(11);
    builder.setCommand(2100);

    builder.setVersion(13);
    builder.setSign("sign");
    builder.setDem(1);
    builder.setTimestamp(System.currentTimeMillis());
    builder.setPkgId("pkgId");
    builder.setFrom("jack@t.email");
    builder.setTo("sean@t.email");
    builder.setSenderPK("SenderPK123");
    builder.setReceiverPK("ReceiverPK456");

    CDTPBody cdtpBody = new CDTPBody();
    cdtpBody.setHeader(new HashMap<>());
    cdtpBody.setQuery(new HashMap<>());
    cdtpBody.setBody(new HashMap<>());

    Map<String, Object> body = cdtpBody.getBody();
    body.put("groupName", "groupName@t.email");
    body.put("groupTemail", "groupName@t.email");
    body.put("pubKey", "pubkey");
    body.put("temail", "jack@t.email");

    Gson gson = new Gson();
    String cdtpBodygsonString = gson.toJson(cdtpBody);
    builder.setData(ByteString.copyFrom(cdtpBodygsonString, Charset.defaultCharset()));
    CDTPPackage ctPackage = builder.build();

    return ctPackage;

  }


  // 在群聊中发消息
  private CDTPPackage sendMsgToGroupTemailBody() {

    CDTPPackage.Builder builder = CDTPPackage.newBuilder();
    builder.setAlgorithm(11);
    builder.setCommand(2000);

    builder.setVersion(13);
    builder.setSign("sign");
    builder.setDem(1);
    builder.setTimestamp(System.currentTimeMillis());
    builder.setPkgId("pkgId");
    builder.setFrom("jack@t.email");
    builder.setTo("sean@t.email");
    builder.setSenderPK("SenderPK123");
    builder.setReceiverPK("ReceiverPK456");

    CDTPBody cdtpBody = new CDTPBody();
    cdtpBody.setHeader(new HashMap<>());
    cdtpBody.setQuery(new HashMap<>());
    cdtpBody.setBody(new HashMap<>());

    Map<String, Object> body = cdtpBody.getBody();
    body.put("from", "jack@t.email");
    body.put("message", "在群聊中发消息****************");
    body.put("to", "groupName@t.email");
    body.put("type", 0);

    Gson gson = new Gson();
    String cdtpBodygsonString = gson.toJson(cdtpBody);
    builder.setData(ByteString.copyFrom(cdtpBodygsonString, Charset.defaultCharset()));
    CDTPPackage ctPackage = builder.build();

    return ctPackage;
  }


}
