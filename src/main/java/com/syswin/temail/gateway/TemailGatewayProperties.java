package com.syswin.temail.gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "temail.gateway")
public class TemailGatewayProperties {

  private String verifyUrl;

  private String dispatchUrl;

  private String consumerGroup;

  private String namesrvAddr;

  // private String topic;

  private String updateSocketStatusUrl;

  private int port;

  private int allowLoseCount;

  private int readIdleTimeSeconds;

  private String mqTopic;


}
