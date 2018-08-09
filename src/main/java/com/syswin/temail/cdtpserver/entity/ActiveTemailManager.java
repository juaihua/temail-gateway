package com.syswin.temail.cdtpserver.entity;

import io.netty.channel.socket.SocketChannel;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by weis on 18/8/8.
 */
public class ActiveTemailManager {

    //在线temail管理
    private static Map<String,TemailInfo> onlineTemailMap = new ConcurrentHashMap<String,TemailInfo>();

    public static void add(String temailKey,TemailInfo temailInfo){
         onlineTemailMap.put(temailKey,temailInfo);

    }

    public static TemailInfo get(String temailKey){

        return  onlineTemailMap.get(temailKey);

    }

    /**
     * 主动离线时调用
     * @param temailInfo
     */
    public static void remove(TemailInfo temailInfo){
        onlineTemailMap.remove(temailInfo.getTemail());
    }

    /**
     * 主动离线时调用
     * @param temailKey
     */
    public static void remove(String temailKey){
        onlineTemailMap.remove(temailKey);
    }

    /**
     * socket 断开时调用(被动离线)
     * @param socketChannel
     */
    public static void removeChannel(SocketChannel socketChannel){
     
      for(Iterator<Map.Entry<String,TemailInfo>> iter = onlineTemailMap.entrySet().iterator(); iter.hasNext();){
        Map.Entry<String,TemailInfo> item = iter.next();
        TemailInfo  temailInfo = item.getValue();
        if(temailInfo.getSocketChannel().remoteAddress().toString().equals(socketChannel.remoteAddress().toString())){
          iter.remove();            
        }       
      }
    }

    public static int getSize(){
        return  onlineTemailMap.size();
    }


}
