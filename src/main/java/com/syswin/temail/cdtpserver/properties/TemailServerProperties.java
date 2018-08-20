package com.syswin.temail.cdtpserver.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@ConfigurationProperties(prefix = "temail")
public class TemailServerProperties {

  private String verifyUrl;

  private String dispatchUrl;

  private String consumerGroup;

  private String namesrvAddr;

  //private String topic;
  
  private String  updateSocketStatusUrl;
  
  private int  port;
  
  private  int   allow_lose_count;
  
  private  int   allIdleTimeSeconds;
  
}
