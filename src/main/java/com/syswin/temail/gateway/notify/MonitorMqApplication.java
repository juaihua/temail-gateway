package com.syswin.temail.gateway.notify;

import com.syswin.temail.gateway.TemailGatewayInstance;
import com.syswin.temail.gateway.TemailGatewayProperties;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Slf4j
public class MonitorMqApplication implements ApplicationRunner {

  @Resource
  private TemailGatewayProperties properties;
  @Resource
  private TemailServerMqListener temailServerMqListener;
  @Resource
  private TemailGatewayInstance gatewayInstance;

  @Override
  public void run(ApplicationArguments args) throws MQClientException {
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getConsumerGroup());
    consumer.setNamesrvAddr(properties.getNamesrvAddr());
    consumer.subscribe(gatewayInstance.getMqTopic(), gatewayInstance.getMqTag());
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    consumer.setMessageListener(temailServerMqListener);
    consumer.start();
    log.info("推送队列监听程序已经启动，开始监听mqTopic:{}, mqTag:{} 队列中的信息！",
        gatewayInstance.getMqTopic(), gatewayInstance.getMqTag());
  }


}
