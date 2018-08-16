package com.syswin.temail.cdtpserver;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Repository;


@Data
@ConfigurationProperties(prefix = "temail")
public class TemailServerProperties {

  private String verifyUrl;

  private String dispatchUrl;

  private String consumerGroup;

  private String namesrvAddr;

  private String topic;


}
