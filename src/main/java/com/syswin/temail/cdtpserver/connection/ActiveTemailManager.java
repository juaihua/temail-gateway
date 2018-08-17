package com.syswin.temail.cdtpserver.connection;

import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.utils.TemailKeyUtil;

import io.netty.channel.socket.SocketChannel;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

/**
 * Created by weis on 18/8/8.
 */
public class ActiveTemailManager {

  // 在线temail管理
  @Getter
  private static Map<String, Map<String, TemailInfo>> onlineTemailMap =
      new ConcurrentHashMap<String, Map<String, TemailInfo>>();

  public static void add(String temailKey, TemailInfo temailInfo) {

    Map<String, TemailInfo> temailMap = onlineTemailMap.get(temailInfo.getTemail());
    if (temailMap != null) {
      temailMap.put(temailInfo.getDevId(), temailInfo);
    } else {
      Map<String, TemailInfo> devidMap = new ConcurrentHashMap<String, TemailInfo>();
      devidMap.put(temailInfo.getDevId(), temailInfo);
      onlineTemailMap.put(temailInfo.getTemail(), devidMap);
    }

  }

  public static TemailInfo getOne(String temailKey) {
    String[] temailKeyArray = temailKey.split("-");
    String temail = temailKeyArray[0];
    String devId = temailKeyArray[1];
    if(null != onlineTemailMap.get(temail)){
      return onlineTemailMap.get(temail).get(devId);
    }
    else{
      return  null;
    }
    // return onlineTemailMap.get(temailKey).get(temailKeyArray[0]);

  }


  public static Map<String, TemailInfo> getAll(String temailKey) {
    return onlineTemailMap.get(TemailKeyUtil.getTemailFromTemailKey(temailKey));
  }


  /**
   * 主动离线时调用
   * 
   * @param temailInfo
   */
  public static void remove(TemailInfo temailInfo) {
    onlineTemailMap.remove(temailInfo.getTemail());    
  }

  /**
   * 主动离线时调用
   * 
   * @param temailKey
   */
  public static void remove(String temailKey) {
    //onlineTemailMap.remove(temailKey);
    onlineTemailMap.remove(TemailKeyUtil.getTemailFromTemailKey(temailKey));
  }

  /**
   * socket 断开时调用(被动离线)
   * 
   * @param socketChannel
   */
  public static void removeChannel(SocketChannel socketChannel) {

    /*
     * for(Iterator<Map.Entry<String,TemailInfo>> iter = onlineTemailMap.entrySet().iterator();
     * iter.hasNext();){ Map.Entry<String,TemailInfo> item = iter.next(); TemailInfo temailInfo =
     * item.getValue();
     * if(temailInfo.getSocketChannel().remoteAddress().toString().equals(socketChannel
     * .remoteAddress().toString())){ iter.remove(); } }
     */
  }

  public static int getSize() {
    return onlineTemailMap.size();
  }


}
