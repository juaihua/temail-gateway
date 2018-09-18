package com.syswin.temail.gateway;

import com.syswin.temail.ps.server.utils.LocalMachineUtil;
import java.util.UUID;
import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "temail.gateway")
public class TemailGatewayProperties {

  private String verifyUrl;
  private String dispatchUrl;
  private String updateSocketStatusUrl;

  private Netty netty = new Netty();
  private Rocketmq rocketmq = new Rocketmq();
  private Instance instance = new Instance();

  @Data
  public static class Netty {

    private int port;
    private int readIdleTimeSeconds;

    public Netty() {
    }
  }

  @Data
  public static class Rocketmq {

    private String namesrvAddr;
    private String consumerGroup;
    /**
     * 持有客户端链句柄的服务实例监听的消息队列topic
     */
    @Setter
    private String mqTopic;

    public Rocketmq() {
    }
  }

  @Data
  public static class Instance {

    private static final String INSTANCE_UNIQUE_TAG_4_HEARTBEAT = "_instance_unique_tag_4_heart_beat_$";

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

    private Instance() {
      hostOf = LocalMachineUtil.getLocalIp();
      //processId = LocalMachineUtil.getLocalProccesId();
      processId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
      System.setProperty(INSTANCE_UNIQUE_TAG_4_HEARTBEAT,processId);
      mqTag = "temail-server-" + hostOf + "-" + processId;
    }

  }
}
