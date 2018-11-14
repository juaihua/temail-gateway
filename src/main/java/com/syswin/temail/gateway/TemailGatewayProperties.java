package com.syswin.temail.gateway;

import com.syswin.temail.ps.server.utils.LocalMachineUtil;
import java.util.UUID;
import javax.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@ConfigurationProperties(prefix = "app.gateway")
@Component
public class TemailGatewayProperties {

  private String verifyUrl;
  private String dispatchUrl;
  private String updateSocketStatusUrl;

  private String grpcServerHost;
  private String grpcServerPort;
  // TODO 这个选项是为了在客户端未完成时，把代码更新到服务器而不影响客户端的功能使用。功能正式上线后选项要删除。
  private boolean groupPacketEnabled = false;

  private Netty netty = new Netty();

  @Resource
  private Rocketmq rocketmq;
  private Instance instance = new Instance();

  @Data
  public static class Netty {

    private int port;
    private int readIdleTimeSeconds = 180;
  }

  @Data
  @Component
  @ConfigurationProperties(prefix = "spring.rocketmq")
  public static class Rocketmq {

    private String namesrvAddr;
    private String consumerGroup;
    /**
     * 持有客户端链句柄的服务实例监听的消息队列topic
     */
    private String mqTopic;
  }


  @Data
  public static class Instance {

    /**
     * 持有客户端链句柄的服务实例宿主机地址
     */
    private String hostOf;
    /**
     * 持有客户端链句柄的服务实例的进程号
     */
    private String processId;
    /**
     * 持有客户端链句柄的服务实例监听的消息队列mqTag
     */
    private String mqTag;

    public Instance() {
      hostOf = LocalMachineUtil.getLocalIp();
      processId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
      mqTag = "temail-server-" + hostOf + "-" + processId;
    }

  }
}
