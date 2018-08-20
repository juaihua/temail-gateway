package com.syswin.temail.cdtpserver.notify;

import java.lang.invoke.MethodHandles;

import lombok.Getter;
import lombok.Setter;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;

public class MonitorMqRunnable implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());
  
  @Setter
  @Getter
  private TemailServerProperties temailServerConfig;
  
  @Setter
  @Getter
  private  TemailMqInfo  temailMqInfo;
  
  
  @Override
  public void run() {
    
    try{     
      LOGGER.info("开始监听MQ:{} 队列中的信息.", temailMqInfo.getMqTopic());       
      DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(temailServerConfig.getConsumerGroup());
      consumer.setNamesrvAddr(temailServerConfig.getNamesrvAddr());
      consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);            
      consumer.subscribe(temailMqInfo.getMqTopic() , "*");
      //consumer.subscribe("temail-server-channle-1" , "*");
      //consumer.subscribe("temail-server-172-31-243-110-7460", "*");
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
