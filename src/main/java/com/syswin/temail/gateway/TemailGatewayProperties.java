package com.syswin.temail.gateway;

import com.syswin.temail.gateway.utils.LocalMachineUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "temail.gateway")
public class TemailGatewayProperties {

  private String verifyUrl;

  private String dispatchUrl;

  private String consumerGroup;

  private String namesrvAddr;

  private String updateSocketStatusUrl;

  private int port;

  private int allowLoseCount;

  private int readIdleTimeSeconds;

  /**
   * 持有客户端链句柄的服务实例宿主机地址
   */
  private String hostOf;
  /**
   * 持有客户端链句柄的服务实例的进程号
   */
  private String processId;
  /**
   * 持有客户端链句柄的服务实例监听的消息队列topic
   */
  private String mqTopic;
  /**
   * 持有客户端链句柄的服务实例监听的消息队列mqTag
   */
  private String mqTag;

  {
    hostOf = LocalMachineUtil.getLocalIp();
    processId = LocalMachineUtil.getLocalProccesId();
    mqTag = "temail-server-" + hostOf + "-" + processId;
  }

}
