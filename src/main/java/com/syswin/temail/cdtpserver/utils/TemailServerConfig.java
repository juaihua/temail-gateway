package com.syswin.temail.cdtpserver.utils;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "login")
public class TemailServerConfig {

  //private String  verifyUrl = "http://172.31.245.225:8888/verify";
  private String  verifyUrl;
  
}
