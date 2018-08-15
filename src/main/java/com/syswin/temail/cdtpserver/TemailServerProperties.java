package com.syswin.temail.cdtpserver;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Repository;


@Data
@ConfigurationProperties(prefix = "temail")
public class TemailServerProperties {

  //private String  verifyUrl = "http://172.31.245.225:8888/verify";
  private String verifyUrl;

  private String dispatchUrl;

  private String consumerGroup;

  private String namesrvAddr;

  private String topic;


}
