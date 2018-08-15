package com.syswin.temail.cdtpserver.notify;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Configuration
public class RocketMqConfiguration {
    /*@Bean
    public MQConsumer consumer(RocketProperties properties, MQProducer producer) throws Exception {      
        String  namesrvAddr = "172.28.43.18:9876";
        String  topic = "cdtp-notify";
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getConsumerGroup());
        //consumer.setNamesrvAddr(properties.getNamesrvAddr());
        consumer.setNamesrvAddr(namesrvAddr);
        //consumer.subscribe(properties.getTopic(), "*");
        consumer.subscribe(topic, "*");
        consumer.setMessageListener(new DispatchListener(producer));
        consumer.start();
        return consumer;
    }*/
}
