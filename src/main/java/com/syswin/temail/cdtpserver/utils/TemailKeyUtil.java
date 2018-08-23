package com.syswin.temail.cdtpserver.utils;

import com.syswin.temail.cdtpserver.constants.TemailConstant;
import com.syswin.temail.cdtpserver.entity.TemailInfo;

public class TemailKeyUtil {

  public static String builderTemailKey(TemailInfo temailInfo) {
    String temailKey =
        temailInfo.getTemail() + TemailConstant.TEMAIL_KEY_SEPARATOR + temailInfo.getDevId();
    return temailKey;
  }

  public static String getTemailFromTemailKey(String temailKey) {
    String[] temailKeyArray = temailKey.split(TemailConstant.TEMAIL_KEY_SEPARATOR);
    /*
     * String temail = temailKeyArray[0]; String devId = temailKeyArray[1];
     */
    return temailKeyArray[0];
  }

  public static String getDevIdFromTemailKey(String temailKey) {
    String[] temailKeyArray = temailKey.split(TemailConstant.TEMAIL_KEY_SEPARATOR);
    /*
     * String temail = temailKeyArray[0]; String devId = temailKeyArray[1];
     */
    return temailKeyArray[1];
  }
  
  
  public  static  void  main(String  args[]){
    System.out.println(getTemailFromTemailKey("jack@t.email"));
  }

}
