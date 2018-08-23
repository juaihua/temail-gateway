package com.syswin.temail.cdtpserver.connection;

import com.syswin.temail.cdtpserver.constants.TemailConstant;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.utils.TemailKeyUtil;

import io.netty.channel.socket.SocketChannel;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by weis on 18/8/8.
 */
@Slf4j
public class ActiveTemailManager {

  // 在线temail管理
  @Getter
  private static Map<String, Map<String, TemailInfo>> onlineTemailMap =
      new ConcurrentHashMap<String, Map<String, TemailInfo>>();

  public static void add(String temailKey, TemailInfo temailInfo) {
    log.info("add  temailInfo to onlineTemailMap,  the temail key is {} ,  TemailInfo  is {}",
        temailKey, temailInfo.toString());
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
    log.info("get Temail from  onlineTemailMap  by  temail key {}", temailKey);
    String[] temailKeyArray = temailKey.split(TemailConstant.TEMAIL_KEY_SEPARATOR);
    String temail = temailKeyArray[0];
    String devId = temailKeyArray[1];
    if (null != onlineTemailMap.get(temail)) {
      return onlineTemailMap.get(temail).get(devId);
    } else {
      return null;
    }
    // return onlineTemailMap.get(temailKey).get(temailKeyArray[0]);

  }


  public static Map<String, TemailInfo> getAll(String temailKey) {
    log.info("get  temailinfo Map  by  temial key: {}", temailKey);
    return onlineTemailMap.get(TemailKeyUtil.getTemailFromTemailKey(temailKey));
  }


  /**
   * 主动离线时调用
   */
  public static void remove(TemailInfo temailInfo) {
    onlineTemailMap.remove(temailInfo.getTemail());
  }

  /**
   * 根据temailKey 从onlineTemailMap 中清除相关的temailInf
   */
  public static void removeByTemailKey(String temailKey) {
    log.info("remove temail info  from  onlineTemailMap by  temail key :{}", temailKey);    
    String[] temailKeyArray = temailKey.split(TemailConstant.TEMAIL_KEY_SEPARATOR);
    String temail = temailKeyArray[0];
    String devId = temailKeyArray[1];   
    //onlineTemailMap.remove(TemailKeyUtil.getTemailFromTemailKey(temailKey));
    
    Map<String, TemailInfo>  devIdTemailInfoMap =  onlineTemailMap.get(temail);
    if(null != devIdTemailInfoMap && (!devIdTemailInfoMap.isEmpty())){
        devIdTemailInfoMap.remove(devId);
        log.info("从onlineTemailMap中 移除 temail is:{} , devId is: {} 的 temailInfo", temail, devId);
        if(devIdTemailInfoMap.isEmpty()){
            onlineTemailMap.remove(temail);
            log.info("temail is :{} 相关的temailInf 在onlineTemailMap 为空, 把对应的devIdTemailInfoMaponlineTemailMap清除", temail);
        }           
    }        
  }

  /**
   * socket 断开时调用(被动离线)
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
