package com.syswin.temail.cdtpserver.entity;

import io.netty.channel.socket.SocketChannel;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by weis on 18/8/8.
 */
@Data
public class TemailInfo {

    //设备Id
    private String devId;

    private String pushToken;

    private String platform;

    private String osVer;

    private String appVer;

    private String lang;

    private String temail;

    private String chl;

    private SocketChannel socketChannel;
    
    private Timestamp timestamp;

    public boolean equals(Object obj){

        if(!(obj instanceof TemailInfo)){
            return false;
        }

        if(this == obj){
            return true;
        }

        return  this.getDevId() == ((TemailInfo)obj).getDevId() && this.getPushToken() == ((TemailInfo)obj).getPushToken() && this.getTemail() == ((TemailInfo)obj).getTemail();
    }

    public int hashCode(){

        int result = 17;
        result = 31 * result + this.getPushToken().hashCode();
        result = 31 * result + this.getTemail().hashCode();
        return result;

    }

}
