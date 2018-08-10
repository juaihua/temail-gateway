package com.syswin.temail.cdtpserver.network;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.entity.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.handler.BaseHandler;
import com.syswin.temail.cdtpserver.handler.HandlerFactory;
import com.syswin.temail.cdtpserver.utils.CommandEnum;
import com.syswin.temail.cdtpserver.utils.SendMsg;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Timestamp;

/**
 * Created by weis on 18/8/2.
 */
@ChannelHandler.Sharable
public class TemailServerHandler extends ChannelInboundHandlerAdapter {


    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private AttributeKey<String> TEMAIL_KEY = AttributeKey.valueOf("TEMAIL_KEY");
    //heartbeat lose counter
    private int counter;

    private BaseHandler handler;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof CDTPPackage) {

            CDTPPackage cdtpPackage = (CDTPPackage) msg;

            if (cdtpPackage.getCommand() == CommandEnum.ping.getCode()) {
                //如果是心跳包
                handleHeartbreat(ctx, cdtpPackage);
            } else {
                handleData(ctx, cdtpPackage);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        System.out.println("client is active");
        ClientMap.add(ctx.channel().remoteAddress().toString(), (SocketChannel) ctx.channel());

        LOGGER.info("当前连接数: ", ClientMap.getSize());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("client is inactive");
        ClientMap.remove((SocketChannel) ctx.channel());
        //System.out.println("当前连接数: "+ClientMap.getSize());
        LOGGER.info("当前连接数: ", ClientMap.getSize());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {//指定时间内,通道没有任何数据
            if (counter >= 3) {
                //close channel
                ctx.channel().close().sync();
                //清理状态数据
                ActiveTemailManager.remove(ctx.channel().attr(TEMAIL_KEY).get());
                System.out.println("已与Client断开连接");
            } else {
                counter++;
                SendMsg.sendHeartbeat((SocketChannel) ctx.channel(), CommandEnum.ping);
                System.out.println("lose: " + counter + "heartbeat packet");
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
     * @param ctx    上下文
     * @param packet 包内容
     */
    private void handleHeartbreat(ChannelHandlerContext ctx, CDTPPackage packet) {

        counter = 0;

        if (packet.getCommand() == CommandEnum.ping.getCode()) {

            SendMsg.sendHeartbeat((SocketChannel) ctx.channel(), CommandEnum.pong);
        }

        LOGGER.info("server received heartbeat packet from:" + ctx.channel().remoteAddress().toString());
    }

    /**
     * 处理业务数据包
     *
     * @param ctx    上下文
     * @param cdtpPackage 包内容
     */
    private void handleData(ChannelHandlerContext ctx, CDTPPackage cdtpPackage) {
        counter = 0;
        //登陆单独处理
        if(cdtpPackage.getCommand() == CommandEnum.connect.getCode()){
            Gson gson = new Gson();
            TemailInfo temailInfo = gson.fromJson(cdtpPackage.getData().toStringUtf8(),TemailInfo.class);
            boolean loginResult = login(cdtpPackage, temailInfo);
            if(loginResult){
                //设置session
                String temailKey = temailInfo.getTemail()+"-"+temailInfo.getDevId();
                ctx.channel().attr(TEMAIL_KEY).set(temailKey);
                temailInfo.setSocketChannel((SocketChannel)ctx.channel());
                temailInfo.setTimestamp(new Timestamp(System.currentTimeMillis()));
                //
                ActiveTemailManager.add(temailKey,temailInfo);
            }else{
              
            }
        }
        else{
          //业务处理
          handler = HandlerFactory.getHandler(cdtpPackage, (SocketChannel) ctx.channel());          
          handler.process();
          cdtpPackage.getCommand();//
          
          System.out.println("***cdtpPackage:" + cdtpPackage.toString());
          ctx.writeAndFlush(cdtpPackage);
          
        }
        
    }

    /**
     * 登陆逻辑
     * @param cdtpPackage
     * @return
     */
    private boolean login(CDTPPackage cdtpPackage, TemailInfo temailInfo){
      
        String  temailKey = temailInfo.getTemail()+"-"+ temailInfo.getDevId();
        if(ActiveTemailManager.get(temailKey) != null){
            //已经在线,不让登陆
            return false;
        }

        return true;
    }

}
