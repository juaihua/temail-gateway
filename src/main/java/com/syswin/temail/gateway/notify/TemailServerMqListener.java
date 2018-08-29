package com.syswin.temail.gateway.notify;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
public class TemailServerMqListener implements MessageListenerConcurrently {

  private final MessageHandler messageHandler;

  public TemailServerMqListener(MessageHandler messageHandler) {
    this.messageHandler = messageHandler;
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
      ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : messages) {
        messageHandler.onMessageReceived(new String(msg.getBody()));
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception ex) {
      log.error("队列传输出错！请求参数：{}", messages, ex);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

}
