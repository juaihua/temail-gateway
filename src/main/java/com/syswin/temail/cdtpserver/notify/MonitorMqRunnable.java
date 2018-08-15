package com.syswin.temail.cdtpserver.notify;

import java.lang.invoke.MethodHandles;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorMqRunnable implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());
  
  @Override
  public void run() {
    
    try{     
      LOGGER.info("开始监听MQ信息........");       
      String  consumerGroup = "cdtp-server-consumer";
      String  namesrvAddr = "172.28.43.18:9876";
      String  topic = "temail-server-channle-1";
      DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
      //consumer.setNamesrvAddr(properties.getNamesrvAddr());
      consumer.setNamesrvAddr(namesrvAddr);
      //consumer.subscribe(properties.getTopic(), "*");
      consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);      
      consumer.subscribe(topic, "*");     
      consumer.setMessageListener(new TemailServerMqListener());      
      consumer.start();
      
    }
    catch (MQClientException ex) {
      LOGGER.error("monitor mq msg  exception", ex);
    }
    catch(Exception ex){
      LOGGER.error("monitor mq msg  exception", ex);
    }       
  }
  
}
