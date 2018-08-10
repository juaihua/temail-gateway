package com.syswin.temail.cdtpserver.client;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;

import com.syswin.temail.cdtpserver.utils.CommandEnum;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by weis on 18/8/3.
 */
@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<String> {
    private Gson gson = new Gson();
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //当被通知Channel是活跃的时候，发送一条消息
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
      CDTPPackage.Builder builder = CDTPPackage.newBuilder();

      builder.setCommand(CommandEnum.connect.getCode());
      builder.setVersion(3);
      
      CDTPPackage ctPackage = builder.build();
      
      ctx.writeAndFlush(ctPackage);
      }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

//        BaseMsg bm = gson.fromJson(msg,BaseMsg.class);
//        System.out.println(
//                "Client received: " + bm.toString()
//        );
//        Thread.sleep(6000);
//        BaseMsg baseMsg = new BaseMsg();
//        baseMsg.setCommand(1);
//        baseMsg.setFrom("weisheng@temail.com");
//        baseMsg.setTo("gaojianhui@temail.com");
//        baseMsg.setVersion("1.0.0");
//        String jsonStr = gson.toJson(baseMsg);
//
//        StringBuffer sb = new StringBuffer(jsonStr);
//        sb.append("\n");
//        ctx.writeAndFlush(sb);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
