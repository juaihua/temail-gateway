package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * heart beat Util for grpc client
 */
@Slf4j
@Data
public class GrpcHeartBeatManager {

  private TemailGatewayProperties temailGatewayProperties;

  private Object heartBeatSingleLock = new Object();

  private ScheduledExecutorService executorService;

  private AtomicBoolean isHeartBeatKeeping = new AtomicBoolean(false);

  private GatewayServer gatewayServer;

  private String instanceProcessId;

  private int heartBeatDelay = 20;

  private GrpcClient grpcClient;

  private String instanceIp;

  public GrpcHeartBeatManager(GrpcClient grpcClient, TemailGatewayProperties temailGatewayProperties) {
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    this.temailGatewayProperties = temailGatewayProperties;
    this.instanceProcessId = temailGatewayProperties.getInstance().getProcessId();
    this.instanceIp = temailGatewayProperties.getInstance().getHostOf();
    this.gatewayServer = GatewayServer.newBuilder().setProcessId(instanceProcessId).setIp(instanceIp).build();
    this.grpcClient = grpcClient;
  }

  /**
   * heart beat logic
   *
   * @param consumer
   */
  public void heartBeat(Consumer<Boolean> consumer) {
    // so the heart beat task will be submitted for only one time;
    if (isHeartBeatKeeping.compareAndSet(false, true)) {
      log.info("heart beat is begining.");
      executorService.scheduleWithFixedDelay(() -> {
        try {
          if (grpcClient.serverHeartBeat(gatewayServer)) {
            consumer.accept(Boolean.TRUE);
            log.info("heart beat result success.");
          } else {
            consumer.accept(Boolean.FALSE);
            throw new IllegalAccessException();
          }
        } catch (Exception e) {
          log.error("heart beat fail, try again after {} seconds .", heartBeatDelay);
        }
      }, heartBeatDelay, heartBeatDelay, TimeUnit.SECONDS);
    } else {
      log.info("heart beat task has already been triggered, {} will leave!", Thread.currentThread().getId());
    }
  }
}
