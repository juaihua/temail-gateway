package com.syswin.temail.cdtpserver.utils;

import com.syswin.temail.cdtpserver.entity.TemailInfo;

public class TemailKeyUtil {

  public static String builderTemailKey(TemailInfo temailInfo) {
    String temailKey = temailInfo.getTemail() + "-" + temailInfo.getDevId();
    return temailKey;
  }

  public static String getTemailFromTemailKey(String temailKey) {
    String[] temailKeyArray = temailKey.split("-");
    /*
     * String temail = temailKeyArray[0]; String devId = temailKeyArray[1];
     */
    return temailKeyArray[0];
  }

  public static String getDevIdFromTemailKey(String temailKey) {
    String[] temailKeyArray = temailKey.split("-");
    /*
     * String temail = temailKeyArray[0]; String devId = temailKeyArray[1];
     */
    return temailKeyArray[1];
  }

}
