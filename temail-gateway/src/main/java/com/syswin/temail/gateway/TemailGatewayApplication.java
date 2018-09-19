package com.syswin.temail.gateway;

import com.syswin.temail.gateway.codec.CommandAwareBodyExtractor;
import com.syswin.temail.gateway.service.RemoteStatusService;
import com.syswin.temail.gateway.service.RequestServiceImpl;
import com.syswin.temail.gateway.service.SessionServiceImpl;
import com.syswin.temail.gateway.service.SilentResponseErrorHandler;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.server.connection.PsServer;
import com.syswin.temail.ps.server.service.ChannelHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * @author 姚华成
 * @date 2018/8/7
 */
@EnableConfigurationProperties({TemailGatewayProperties.class})
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

  @Bean
  ChannelHolder channelHolder(
      TemailGatewayProperties properties,
      RestTemplate restTemplate,
      WebClient webClient) {

    SessionServiceImpl sessionService =
        new SessionServiceImpl(restTemplate, properties,
            new RemoteStatusService(properties, webClient));
    PsServer psServer =
        new PsServer(
            sessionService,
            new RequestServiceImpl(webClient, properties),
            new CommandAwareBodyExtractor(
                new SimpleBodyExtractor()));
    psServer.run(properties.getNetty().getPort(), properties.getNetty().getReadIdleTimeSeconds());

    return sessionService.getChannelHolder();
  }
}
