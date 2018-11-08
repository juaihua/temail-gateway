package com.syswin.temail.gateway.notify;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.codec.CommandAwarePacketUtil;
import com.syswin.temail.ps.server.service.ChannelHolder;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * @author 姚华成
 * @date 2018-11-07
 */
public class RocketMqRunner implements ApplicationRunner, Ordered {

  private TemailGatewayProperties properties;
  private ChannelHolder channelHolder;

  public RocketMqRunner(TemailGatewayProperties properties, ChannelHolder channelHolder) {
    this.properties = properties;
    this.channelHolder = channelHolder;
  }

  @Override
  public void run(ApplicationArguments args) throws MQClientException {
    RocketMqConsumer consumer = new RocketMqConsumer(properties,
        new TemailServerMqListener(
            new MessageHandler(channelHolder, new CommandAwarePacketUtil(properties))));

    consumer.start();
    Runtime.getRuntime().addShutdownHook(new Thread(consumer::stop));
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
