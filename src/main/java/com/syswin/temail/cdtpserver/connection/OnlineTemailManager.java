package com.syswin.temail.cdtpserver.connection;

import com.syswin.temail.cdtpserver.entity.TemailInfo;
import io.netty.channel.socket.SocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by weis on 18/8/8.
 */
public class OnlineTemailManager {

    //在线temail管理
   private static Map<String, TemailInfo> onlineTemailMap = new ConcurrentHashMap<String,TemailInfo>();

    //socketChannel 对应多个Temail
    private static Map<String,List<TemailInfo>> activeChannelMap = new ConcurrentHashMap<String,List<TemailInfo>>();


    public static void add(String from,TemailInfo temailInfo){

         onlineTemailMap.put(from,temailInfo);

        String activeChannelKey = temailInfo.getSocketChannel().remoteAddress().toString();

        if( activeChannelMap.get(activeChannelKey) == null){

            List<TemailInfo> list = new ArrayList<TemailInfo>();
            list.add(temailInfo);

            activeChannelMap.put(activeChannelKey,list);
        }else{
            List<TemailInfo> list = activeChannelMap.get(activeChannelKey);
            list.add(temailInfo);
        }

    }

    public static TemailInfo get(String from){

        return  onlineTemailMap.get(from);

    }

    /**
     * 主动离线时调用
     * @param temailInfo
     */
    public static void remove(TemailInfo temailInfo){

//        for(Map.Entry<String,TemailInfo> entry:activeTemailMap.entrySet()){
//            if(entry.getValue().equals(temailInfo)){
//                activeTemailMap.remove(entry.getKey());
//            }
//        }

         onlineTemailMap.remove(temailInfo.getTemail());

        String activeChannelKey = temailInfo.getSocketChannel().remoteAddress().toString();
        List<TemailInfo> list = activeChannelMap.get(activeChannelKey);
        if(list != null){
            list.remove(temailInfo);
        }
    }

    /**
     * socket 断开时调用(被动离线)
     * @param socketChannel
     */
    public static void removeChannel(SocketChannel socketChannel){
        String activeChannelKey = socketChannel.remoteAddress().toString();
        List<TemailInfo> list = activeChannelMap.get(activeChannelKey);
        list.forEach(temailInfo -> {
             onlineTemailMap.remove(temailInfo.getTemail());
        });
        activeChannelMap.remove(activeChannelKey);
    }

    public static int getSize(){
        return  onlineTemailMap.size();
    }


}
