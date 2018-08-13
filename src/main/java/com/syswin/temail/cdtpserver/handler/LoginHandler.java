package com.syswin.temail.cdtpserver.handler;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.entity.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.OnlineTemailManager;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.utils.CommandEnum;
import com.syswin.temail.cdtpserver.utils.ConstantsAttributeKey;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Timestamp;

/**
 * Created by weis on 18/8/8.
 */
public class LoginHandler extends BaseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public LoginHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage){
        super(socketChannel,cdtpPackage);
    }

    @Override
    public void process() {
      /**
       * 1.先判断From合法性
       * 2.调用dispatch服务
       * 3.成功操作状态管理服务
       * 4.失败,返回错误信息,关闭连接
       */
      LOGGER.info("in  LoginHandler receive cdtp msg:"+ getCdtpPackage().toString() );
      Gson gson = new Gson();
      TemailInfo temailInfo = gson.fromJson(getCdtpPackage().getData().toStringUtf8(),TemailInfo.class);
      boolean loginResult = login(getCdtpPackage(), temailInfo);
      if(loginResult){
          //设置session
          String temailKey = temailInfo.getTemail()+"-"+temailInfo.getDevId();
          getSocketChannel().attr(ConstantsAttributeKey.TEMAIL_KEY).set(temailKey);
          //ctx.channel().attr(TEMAIL_KEY).set(temailKey);
          temailInfo.setSocketChannel(getSocketChannel());
          temailInfo.setTimestamp(new Timestamp(System.currentTimeMillis()));               
          ActiveTemailManager.add(temailKey,temailInfo);                
                        
          CDTPPackage.Builder builder = CDTPPackage.newBuilder();               
          builder.setCommand(CommandEnum.resp.getCode());
          builder.setPkgId(getCdtpPackage().getPkgId());
          CDTPPackage newcdtpPackage = builder.build();
          LOGGER.info("send login success msg:"+newcdtpPackage.toString());
          getSocketChannel().writeAndFlush(newcdtpPackage);
          
      }
      
        //login biz
        




     /*   //认证成功后
        TemailInfo temailInfo = new TemailInfo();
        temailInfo.setTemail(this.getCdtpPackage().getFrom());
//        temailInfo.setDeviceId()

        temailInfo.setSocketChannel(this.getSocketChannel());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        temailInfo.setTimestamp(timestamp);//当前登陆时间

        if(OnlineTemailManager.get(temailInfo.getTemail()) != null){
            //已登录,并且是连接可用,返回状态信息
        }else{
            //增加到连接管理
            OnlineTemailManager.add(temailInfo.getTemail()+":"+temailInfo.getDevId() ,temailInfo);
            
            LOGGER.info(temailInfo.getTemail()+"login success.");
        }
*/
    }
    
    /**
     * 登陆逻辑
     * @param cdtpPackage
     * @return
     */
    private boolean login(CDTPPackage cdtpPackage, TemailInfo temailInfo){
      
        /*String  temailKey = temailInfo.getTemail()+"-"+ temailInfo.getDevId();
        if(ActiveTemailManager.get(temailKey) != null){
            //已经在线,不让登陆
            return false;
        }
*/
        return true;
    }
}
