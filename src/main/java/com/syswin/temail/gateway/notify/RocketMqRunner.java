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

  private final RocketMqConsumer consumer;

  public RocketMqRunner(TemailGatewayProperties properties, ChannelHolder channelHolder,
      CommandAwarePacketUtil packetUtil) {
    consumer = new RocketMqConsumer(properties,
        new TemailServerMqListener(
            new MessageHandler(channelHolder, packetUtil)));
  }

  @Override
  public void run(ApplicationArguments args) throws MQClientException {
    consumer.start();
    Runtime.getRuntime().addShutdownHook(new Thread(consumer::stop));
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
