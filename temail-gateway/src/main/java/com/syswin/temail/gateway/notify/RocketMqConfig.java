package com.syswin.temail.gateway.notify;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.ps.server.service.ChannelHolder;
import javax.annotation.PostConstruct;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
class RocketMqConfig {

  @Autowired
  private TemailGatewayProperties properties;

  @Autowired
  private ChannelHolder channelHolder;

  @PostConstruct
  void start() throws MQClientException {
    RocketMqConsumer consumer = new RocketMqConsumer(
        properties,
        new TemailServerMqListener(new MessageHandler(channelHolder)));

    consumer.start();
    Runtime.getRuntime().addShutdownHook(new Thread(consumer::stop));
  }
}
