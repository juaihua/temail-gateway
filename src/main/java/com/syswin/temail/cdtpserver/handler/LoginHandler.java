package com.syswin.temail.cdtpserver.handler;

import com.syswin.temail.cdtpserver.entity.OnlineTemailManager;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
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

    public LoginHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){
        super(socketChannel,cdtpPackage);
    }

    @Override
    public void process() {
        //login biz
        /**
         * 1.先判断From合法性
         * 2.调用dispatch服务
         * 3.成功操作状态管理服务
         * 4.失败,返回错误信息,关闭连接
         */




        //认证成功后
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

    }
}
