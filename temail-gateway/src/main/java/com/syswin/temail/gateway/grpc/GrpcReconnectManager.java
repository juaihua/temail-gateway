package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * reconnet util for grpc client
 */
@Slf4j
@Data
public class GrpcReconnectManager {

  private AtomicBoolean isReconnectWorking = new AtomicBoolean(false);

  private TemailGatewayProperties temailGatewayProperties;

  private GrpcClientWrapper grpcClientWrapper;

  private GrpcClientBuilder grpcClientBuilder;

  private ExecutorService executorService;

  private GatewayServer gatewayServer;

  private int reconnectDelay = 5;

  public GrpcReconnectManager(GrpcClientWrapper grpcClientWrapper,
      TemailGatewayProperties temailGatewayProperties) {
    this.temailGatewayProperties = temailGatewayProperties;
    this.gatewayServer = GatewayServer.newBuilder().setIp(temailGatewayProperties.getInstance().getHostOf())
        .setProcessId(temailGatewayProperties.getInstance().getProcessId()).build();
    this.grpcClientBuilder = new GrpcClientBuilder(temailGatewayProperties.getGrpcServerHost(),
        Integer.parseInt(temailGatewayProperties.getGrpcServerPort()));
    this.executorService = Executors.newSingleThreadExecutor();
    this.grpcClientWrapper = grpcClientWrapper;
  }

  /**
   * be aware of only one reconnect task can be triggered in the same time
   */
  public void reconnect(Consumer consumer) throws IllegalAccessException {
    if (isReconnectWorking.compareAndSet(false, true)) {
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          while (!Thread.currentThread().isInterrupted()) {
            try {
              //keep trying to reconnect by invoke grpcClient.serevrRegistry(). until invoke success.
              if (grpcClientWrapper.getGrpcClient().serverRegistry(gatewayServer)) {
                log.error("reconnect fail, {} seconds try again! ", reconnectDelay);
                throw new IllegalStateException("reconnect fail.");
              }
              log.info("reconnect success.");
              grpcClientWrapper.reconnectSuccessful();
              if (consumer != null) {
                consumer.accept(Boolean.TRUE);
              }
              isReconnectWorking.compareAndSet(true, false);
              break;
            } catch (Exception e) {
              if (consumer != null) {
                consumer.accept(Boolean.TRUE);
              }
              try {
                TimeUnit.SECONDS.sleep(reconnectDelay);
              } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                e1.printStackTrace();
              }
            }
            log.info("reconnect finish, now exit the reconnect loop! ");
          }
        }
      });
    } else {
      log.warn("more than one reconnect work is not allowed!");
    }
  }
}
