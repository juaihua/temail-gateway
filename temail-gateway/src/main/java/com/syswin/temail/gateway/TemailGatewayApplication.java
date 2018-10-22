package com.syswin.temail.gateway;

import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.channels.clients.grpc.GrpcClientWrapper;
import com.syswin.temail.gateway.codec.CommandAwareBodyExtractor;
import com.syswin.temail.gateway.service.RemoteStatusService;
import com.syswin.temail.gateway.service.RequestServiceHttpClientAsync;
import com.syswin.temail.gateway.service.SessionServiceImpl;
import com.syswin.temail.gateway.service.SilentResponseErrorHandler;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.server.PsServer;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import com.syswin.temail.ps.server.service.ChannelHolder;
import com.syswin.temail.ps.server.service.RequestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
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
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(3000)
        .setReadTimeout(3000)
        .errorHandler(new SilentResponseErrorHandler())
        .build();
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
  public AbstractSessionService sessionService(TemailGatewayProperties properties,
      RestTemplate restTemplate,
      ChannelsSyncClient channelsSyncClient) {
    return new SessionServiceImpl(restTemplate, properties.getVerifyUrl(),
        new RemoteStatusService(properties, channelsSyncClient));
  }

  @Bean
  public RequestService requestService(TemailGatewayProperties properties) {
    // 由于Skywalking不支持WebClient的方式，因此改为HttpClient
    // return new RequestServiceWebClient(properties.getDispatchUrl());
    return new RequestServiceHttpClientAsync(properties.getDispatchUrl());
  }

  @Bean
  ChannelHolder channelHolder(AbstractSessionService sessionService) {
    return sessionService.getChannelHolder();
  }

  @Bean
  TemailGatewayRunner gatewayRunner(TemailGatewayProperties properties,
      AbstractSessionService sessionService,
      RequestService requestService) {
    return new TemailGatewayRunner(
        new PsServer(
            sessionService,
            requestService,
            properties.getNetty().getPort(),
            properties.getNetty().getReadIdleTimeSeconds(),
            new CommandAwareBodyExtractor(
                new SimpleBodyExtractor())));
  }

}
