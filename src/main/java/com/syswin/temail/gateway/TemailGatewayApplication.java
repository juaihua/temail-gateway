package com.syswin.temail.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
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
        .errorHandler(new DefaultResponseErrorHandler() {
          @Override
          public void handleError(ClientHttpResponse clientHttpResponse) {
          }
        })
        .build();
  }

  @Bean("dispatcherWebClient")
  public WebClient dispatcherWebClient(TemailGatewayProperties properties) {
    return WebClient.create(properties.getDispatchUrl());
  }

  @Bean("statusWebClient")
  public WebClient statusWebClient(TemailGatewayProperties properties) {
    return WebClient.create(properties.getUpdateSocketStatusUrl());
  }

}
