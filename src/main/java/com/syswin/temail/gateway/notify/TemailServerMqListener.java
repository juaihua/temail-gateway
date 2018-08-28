package com.syswin.temail.gateway.notify;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.service.ChannelHolder;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Slf4j
//@Component
public class TemailServerMqListener implements MessageListenerConcurrently {

  private Gson gson = new Gson();
  private ChannelHolder channelHolder;

  public TemailServerMqListener(ChannelHolder channelHolder) {
    this.channelHolder = channelHolder;
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
      ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        String msgData = new String(msg.getBody());
        try {
          log.info("*********************************从MQ接受到消息是:" + msgData);
          CDTPPacket packet = null;
          packet = gson.fromJson(msgData, CDTPPacket.class);

          String receiver = packet.getHeader().getReceiver();
          Collection<Channel> channels = channelHolder.getChannels(receiver);
          for (Channel channel : channels) {
            channel.writeAndFlush(packet);
          }
        } catch (JsonSyntaxException e) {
          log.info("接收到不符合格式的消息：{}", msgData);
        }
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception ex) {
      log.error("队列传输出错！请求参数：{}", msgs, ex);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

}
