package com.syswin.temail.cdtpserver.network;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by weis on 18/8/4.
 */
public class ClientMap {

    private static Map<String,SocketChannel> map = new ConcurrentHashMap<>(16);


    public static void add(String clientId,SocketChannel socketChannel){
        map.put(clientId,socketChannel);
    }

    public static Channel get(String clientId){
        return map.get(clientId);
    }

    public static void remove(SocketChannel socketChannel){

        for(Map.Entry<String,SocketChannel> entry:map.entrySet()){
            if(entry.getValue() == socketChannel){
                map.remove(entry.getKey());
            }
        }

    }

    public static int getSize(){
        return map.size();
    }
}
