package com.syswin.temail.cdtpserver.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Repository;


@Repository(value="temailServerConfig")
@Data
@ConfigurationProperties 
public class TemailServerConfig {

  //private String  verifyUrl = "http://172.31.245.225:8888/verify";
  @Value("${temail.verifyUrl}")  
  private String  verifyUrl;
  
  @Value("${temail.dispatchUrl}")
  private String   dispatchUrl;
  
  @Value("${temail.consumerGroup}")
  private String   consumerGroup;
  
  @Value("${temail.namesrvAddr}")
  private String   namesrvAddr;
  
  @Value("${temail.topic}")
  private String   topic;
  
  
}
