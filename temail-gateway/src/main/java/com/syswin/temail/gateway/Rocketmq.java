package com.syswin.temail.gateway;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spring.rocketmq")
@Component
public class Rocketmq {

  private String namesrvAddr;

  private String consumerGroup;
  /**
   * 持有客户端链句柄的服务实例监听的消息队列topic
   */

  private String mqTopic;

  public Rocketmq() {
  }
}
