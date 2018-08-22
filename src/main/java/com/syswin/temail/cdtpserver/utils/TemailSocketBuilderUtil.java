package com.syswin.temail.cdtpserver.utils;

import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketInstance;

public class TemailSocketBuilderUtil {

  
  //builder 状态服务需要的TemailSocketInfo 的信息
  public  static TemailSocketInfo  temailSocketBuilder(TemailInfo temailInf,  TemailMqInfo  temailMqInfo, String  optype){
      TemailSocketInfo  temailSocketInf   = new  TemailSocketInfo();
      temailSocketInf.setAccount(temailInf.getTemail());
      temailSocketInf.setOptype(optype);
      
      TemailSocketInstance  instance = new TemailSocketInstance();
      instance.setDevId(temailInf.getDevId());
      instance.setMqTopic(temailMqInfo.getMqTopic());
      instance.setMqTag(temailMqInfo.getMqTag());
      instance.setHostOf(temailMqInfo.getHostOf());
      instance.setProcessId(temailMqInfo.getProcessId());      
      temailSocketInf.setStatus(instance);
      return  temailSocketInf;
  }
  
  

}
