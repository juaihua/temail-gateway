package com.syswin.temail.gateway;

import com.syswin.temail.gateway.utils.LocalMachineUtil;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Data
@Component
public class TemailGatewayInstance {

  // 持有客户端链句柄的服务实例监听的消息队列topic
  private String mqTopic;
  // 持有客户端链句柄的服务实例监听的消息队列mqTag
  private String mqTag;
  // 持有客户端链句柄的服务实例宿主机地址
  private String hostOf;
  // 持有客户端链句柄的服务实例的进程号
  private String processId;

  public TemailGatewayInstance(TemailGatewayProperties properties) {
    mqTopic = properties.getMqTopic();
    mqTag = "temail-server-" + LocalMachineUtil.getLocalIp() + "-"
        + LocalMachineUtil.getLocalProccesId();
    hostOf = LocalMachineUtil.getLocalIp();
    processId = LocalMachineUtil.getLocalProccesId();
  }
}
