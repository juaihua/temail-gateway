package com.syswin.temail.gateway;

import com.syswin.temail.gateway.codec.CommandAwareBodyExtractor;
import com.syswin.temail.gateway.codec.SimpleBodyExtractor;
import com.syswin.temail.gateway.connection.TemailGatewayServer;
import com.syswin.temail.gateway.service.ChannelHolder;
import com.syswin.temail.gateway.service.RemoteStatusService;
import com.syswin.temail.gateway.service.RequestService;
import com.syswin.temail.gateway.service.SessionService;
import com.syswin.temail.gateway.service.SilentResponseErrorHandler;
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
  ChannelHolder channelCollector(
      TemailGatewayProperties properties,
      RestTemplate restTemplate,
      WebClient webClient) {

    final TemailGatewayServer gatewayServer = new TemailGatewayServer(
        new SessionService(restTemplate, properties, new RemoteStatusService(properties, webClient)),
        new RequestService(webClient, properties),
        new CommandAwareBodyExtractor(new SimpleBodyExtractor()));

    gatewayServer.run(properties.getNetty().getPort(), properties.getNetty().getReadIdleTimeSeconds());
    return gatewayServer.channelHolder();
  }
}
