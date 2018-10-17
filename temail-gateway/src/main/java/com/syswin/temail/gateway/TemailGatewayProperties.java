package com.syswin.temail.gateway;

import com.syswin.temail.ps.server.utils.LocalMachineUtil;
import java.util.UUID;
import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
public class TemailGatewayProperties {

  @Value("url.auth.verifyUrl")
  private String verifyUrl;
  @Value("url.dispatch.dispatchUrl")
  private String dispatchUrl;
  @Value("url.channel.updateSocketStatusUrl")
  private String updateSocketStatusUrl;

  @Value("app.gateway.grpcServerHost")
  private String grpcServerHost;
  @Value("app.gateway.grpcServerPort")
  private String grpcServerPort;

  private Netty netty = new Netty();
  private Rocketmq rocketmq = new Rocketmq();
  private Instance instance = new Instance();

  @Data
  public static class Netty {

    @Value("app.gateway.netty.port")
    private int port;
    @Value("app.gateway.netty.read-idle-time-seconds")
    private int readIdleTimeSeconds;

    public Netty() {
    }
  }

  @Data
  public static class Rocketmq {

    @Value("spring.rocketmq.namesrv-addr")
    private String namesrvAddr;

    @Value("spring.rocketmq.consumer-group")
    private String consumerGroup;
    /**
     * 持有客户端链句柄的服务实例监听的消息队列topic
     */
    @Setter
    @Value("spring.rocketmq.mq-topic")
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

    public Instance() {
      hostOf = LocalMachineUtil.getLocalIp();
      //processId = LocalMachineUtil.getLocalProccesId();
      processId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
      System.setProperty(INSTANCE_UNIQUE_TAG_4_HEARTBEAT,processId);
      mqTag = "temail-server-" + hostOf + "-" + processId;
    }

  }
}
