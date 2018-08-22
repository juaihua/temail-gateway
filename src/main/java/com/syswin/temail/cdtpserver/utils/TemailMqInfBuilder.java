package com.syswin.temail.cdtpserver.utils;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
@Slf4j
@Component
public class TemailMqInfBuilder {

  public  static TemailMqInfo    getTemailMqInf(TemailServerProperties temailServerConfig){     
    String  mqTag =  "temail-server-"+LocalMachineUtil.getLocalIp()+"-"+LocalMachineUtil.getLocalProccesId();
    TemailMqInfo  temailMqInfo  =  new  TemailMqInfo();
    temailMqInfo.setHostOf(LocalMachineUtil.getLocalIp());
    temailMqInfo.setProcessId(LocalMachineUtil.getLocalProccesId());
    temailMqInfo.setMqTopic(temailServerConfig.getMqTopic());
    temailMqInfo.setMqTag(mqTag);    
    //log.info("当前实例监听的MQ的topic是:{}",mqTopic);
    return  temailMqInfo;
 }
}
