package com.syswin.temail.cdtpserver.utils;

import lombok.extern.slf4j.Slf4j;

import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
@Slf4j
public class TemailMqInfBuilder {

  public  static TemailMqInfo    getTemailMqInf(){     
    String  mqTopic =  "temail-server-"+LocalMachineUtil.getLocalIp()+"-"+LocalMachineUtil.getLocalProccesId();
    TemailMqInfo  temailMqInfo  =  new  TemailMqInfo();
    temailMqInfo.setHostOf(LocalMachineUtil.getLocalIp());
    temailMqInfo.setProcessId(LocalMachineUtil.getLocalProccesId());
    temailMqInfo.setMqTopic(mqTopic);
    log.info("当前实例监听的MQ的topic是:{}",mqTopic);
    return  temailMqInfo;
 }
}
