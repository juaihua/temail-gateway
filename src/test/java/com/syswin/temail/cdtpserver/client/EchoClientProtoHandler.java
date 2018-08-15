package com.syswin.temail.cdtpserver.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.CDTPBody;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.utils.CommandEnum;
import com.syswin.temail.cdtpserver.utils.ConstantsAttributeKey;

/**
 * Created by weis on 18/8/3.
 */
@ChannelHandler.Sharable
public class EchoClientProtoHandler extends ChannelInboundHandlerAdapter{
    int  counter = 0;
    private Gson gson = new Gson();
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //当被通知Channel是活跃的时候，发送一条消息
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
      CDTPPackage.Builder builder = CDTPPackage.newBuilder();
      builder.setAlgorithm(1);
      //builder.setCommand(2);
      builder.setCommand(CommandEnum.connect.getCode());
      builder.setPkgId("pckAgeId1234");
      builder.setVersion(3);
      
      TemailInfo temailInfo = new TemailInfo();
      temailInfo.setTemail("sean@t.email");
      temailInfo.setDevId("devId");
      Gson gson = new Gson();
      String gsonString = gson.toJson(temailInfo);
      
      builder.setData(ByteString.copyFrom(gsonString, Charset.defaultCharset()));     
      CDTPPackage ctPackage = builder.build();
      
      ctx.writeAndFlush(ctPackage);
      }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      System.out.println("client  attrKey:"+ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get());
      if(msg instanceof CDTPPackage){
        System.out.println("msg:"+msg);
      }
      if(counter ==0){
      //if(counter <=2){
        if(msg instanceof CDTPPackage){
          System.out.println("msg:"+msg);
          counter++;
          CDTPPackage.Builder builder = CDTPPackage.newBuilder();
          builder.setAlgorithm(11);
          //builder.setCommand(CommandEnum.req.getCode());
          builder.setCommand(1002);
          
          builder.setVersion(13);
          builder.setSign("sign");
          builder.setDem(1);
          builder.setTimestamp(System.currentTimeMillis());
          builder.setPkgId("pkgId");
          builder.setFrom("gaojhg@syswin.com");
          builder.setTo("yaohuacheng@syswin.com");
          builder.setSenderPK("SenderPK(");
          builder.setReceiverPK("ReceiverPK(");
          
                           
          
          CDTPBody cdtpBody   =  initCDTPBody(); 
          Gson gson = new Gson();
          String  cdtpBodygsonString = gson.toJson(cdtpBody);      
          
          builder.setData(ByteString.copyFrom(cdtpBodygsonString, Charset.defaultCharset()));  
          CDTPPackage ctPackage = builder.build(); 
          
          ctx.writeAndFlush(ctPackage);
        } 
      }
      else{
        System.out.println("no send pinginfo ");
      }
           

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    
    private CDTPBody initCDTPBody() {
      CDTPBody cdtpBody = new CDTPBody();
      cdtpBody.setHeader(new LinkedMultiValueMap<>());
      cdtpBody.setQuery(new LinkedMultiValueMap<>());
      cdtpBody.setBody(new HashMap<>());
      
      Map<String, Object> body = cdtpBody.getBody();
      body.put("from", "yaohuacheng@syswin.com");
      body.put("fromMsg", "string");
      body.put("msgid", "syswin-1534131915194-4");
      body.put("seqNo", 3);
      body.put("to", "wangxuanzhong@syswin.com");
      body.put("toMsg", "string");
      body.put("type", 0);
      return cdtpBody;
    }


}
