package com.syswin.temail.gateway;

import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.channels.clients.grpc.GrpcClientWrapper;
import com.syswin.temail.gateway.codec.CommandAwarePacketUtil;
import com.syswin.temail.gateway.notify.RocketMqRunner;
import com.syswin.temail.gateway.service.AuthService;
import com.syswin.temail.gateway.service.AuthServiceHttpClientAsync;
import com.syswin.temail.gateway.service.DispatchService;
import com.syswin.temail.gateway.service.DispatchServiceHttpClientAsync;
import com.syswin.temail.gateway.service.RemoteStatusService;
import com.syswin.temail.gateway.service.RequestServiceImpl;
import com.syswin.temail.gateway.service.SessionServiceImpl;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.server.PsServer;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import com.syswin.temail.ps.server.service.ChannelHolder;
import com.syswin.temail.ps.server.service.RequestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * @author 姚华成
 * @date 2018/8/7
 */
@SpringBootApplication
public class TemailGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(TemailGatewayApplication.class, args);
  }

  @Bean
  public WebClient webClient() {
    return WebClient.create();
  }

  @Bean(initMethod = "initClient", destroyMethod = "destroyClient")
  public ChannelsSyncClient initGrpcClient(TemailGatewayProperties properties) {
    return new GrpcClientWrapper(properties);
  }

  @Bean
  public AuthService loginService(TemailGatewayProperties properties) {
    return new AuthServiceHttpClientAsync(properties.getVerifyUrl());
  }

  @Bean
  public AbstractSessionService sessionService(TemailGatewayProperties properties, AuthService authService,
      ChannelsSyncClient channelsSyncClient) {
    return new SessionServiceImpl(authService,
        new RemoteStatusService(properties, channelsSyncClient));
  }

  @Bean
  ChannelHolder channelHolder(AbstractSessionService sessionService) {
    return sessionService.getChannelHolder();
  }

  @Bean
  public DispatchService dispatchService(TemailGatewayProperties properties) {
    // 由于Skywalking不支持WebClient的方式，因此改为HttpClient
    // return new DispatchServiceWebClient(properties.getDispatchUrl());
    return new DispatchServiceHttpClientAsync(properties.getDispatchUrl());
  }

  @Bean
  public RequestService requestService(DispatchService dispatchService) {
    return new RequestServiceImpl(dispatchService);
  }

  @Bean
  TemailGatewayRunner gatewayRunner(TemailGatewayProperties properties,
      AbstractSessionService sessionService,
      RequestService requestService) {
//    KeyAwareVault vault = VaultKeeper.keyAwareVault("", "");
    return new TemailGatewayRunner(
        new PsServer(
            sessionService,
            requestService,
            properties.getNetty().getPort(),
            properties.getNetty().getReadIdleTimeSeconds(),
            new CommandAwarePacketUtil(properties, SimpleBodyExtractor.INSTANCE)
//            ,
//            new KeyAwareEccPacketSigner(vault),
//            new KeyAwarePacketVerifier(vault),
//            new KeyAwareEccPacketEncryptor(vault)
        ));
  }

  @Bean
  public RocketMqRunner rocketMqRunner(TemailGatewayProperties properties, ChannelHolder channelHolder) {
    return new RocketMqRunner(properties, channelHolder);
  }
}
