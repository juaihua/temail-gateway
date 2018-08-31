package com.syswin.temail.gateway.notify;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.TemailGatewayProperties.Rocketmq;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

@Slf4j
class RocketMqConsumer {

  private final TemailGatewayProperties properties;
  private final MessageListener messageListener;
  private final DefaultMQPushConsumer consumer;

  RocketMqConsumer(TemailGatewayProperties properties, MessageListener messageListener) {
    this.properties = properties;
    this.messageListener = messageListener;
    this.consumer = new DefaultMQPushConsumer(properties.getRocketmq().getConsumerGroup());
  }

  void start() throws MQClientException {
    Rocketmq rocketmq = properties.getRocketmq();
    consumer.setNamesrvAddr(rocketmq.getNamesrvAddr());
    consumer.subscribe(rocketmq.getMqTopic(), properties.getInstance().getMqTag());
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    consumer.setMessageListener(messageListener);
    consumer.start();

    log.info("推送队列监听程序已经启动，开始监听mqTopic:{}, mqTag:{} 队列中的信息！",
        rocketmq.getMqTopic(), properties.getInstance().getMqTag());
  }

  void stop() {
    consumer.shutdown();
  }
}
