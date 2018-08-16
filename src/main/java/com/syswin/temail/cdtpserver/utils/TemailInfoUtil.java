package com.syswin.temail.cdtpserver.utils;

import com.syswin.temail.cdtpserver.entity.TemailInfo;

public class TemailInfoUtil {

    public  static  String   builderTemailKey(TemailInfo temailInfo){
        String temailKey = temailInfo.getTemail() + "-" + temailInfo.getDevId();
        return  temailKey;
    }
  
}
