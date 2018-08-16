package com.syswin.temail.cdtpserver.utils;

import lombok.Getter;

import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketInstance;

public class TemailSocketBuilderUtil {

  
  public  static TemailSocketInfo  temailSocketBuilder(TemailInfo temailInf, String  optype){
      TemailSocketInfo  temailSocketInf   = new  TemailSocketInfo();
      temailSocketInf.setAccount(temailInf.getTemail());
      temailSocketInf.setOptype(optype);
      
      TemailSocketInstance  instance = new TemailSocketInstance();
      instance.setDevId(temailInf.getDevId());
      //instance.setMqTopic(mqTopic);
      instance.setHostOf(LocalMachineUtil.getLocalIp());
      instance.setProcessId(LocalMachineUtil.getLocalProccesId());      
      temailSocketInf.setInstance(instance);
      return  temailSocketInf;
  }
  
  

}
