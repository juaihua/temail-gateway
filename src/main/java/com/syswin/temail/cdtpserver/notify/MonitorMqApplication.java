package com.syswin.temail.cdtpserver.notify;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.utils.TemailMqInfBuilder;

@Component
@Order(2)
@Slf4j
public class MonitorMqApplication implements ApplicationRunner {

  @Resource
  private TemailServerProperties temailServerConfig;

  @Resource
  private TemailServerProperties properties;
  
  @Override
  public void run(ApplicationArguments args) throws Exception {
    TemailMqInfo  temailMqInfo  = TemailMqInfBuilder.getTemailMqInf();     
     log.info("开始监听MQ:{} 队列中的信息.", temailMqInfo.getMqTopic());       
     try{            
       DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(temailServerConfig.getConsumerGroup());
       consumer.setNamesrvAddr(temailServerConfig.getNamesrvAddr());
       consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);            
       //consumer.subscribe(temailMqInfo.getMqTopic() , "*");
       //consumer.subscribe("temail-server-channle-1" , "*");
       consumer.subscribe("temail-server-172-31-243-110-7460", "*");
       consumer.setMessageListener(new TemailServerMqListener());      
       consumer.start();      
     }
     catch (MQClientException ex) {
       log.error("monitor mq msg  exception", ex);
     }
     catch(Exception ex){
       log.error("monitor mq msg  exception", ex);
     }       
     
     
     /*MonitorMqRunnable  monitorMqRunnable =new   MonitorMqRunnable();
     monitorMqRunnable.setTemailServerConfig(properties);
     monitorMqRunnable.setTemailMqInfo(temailMqInfo);
     Thread monitorThread = new Thread(monitorMqRunnable, "monitorThread") ;
     monitorThread.start();*/
  }
  
  
 

}
