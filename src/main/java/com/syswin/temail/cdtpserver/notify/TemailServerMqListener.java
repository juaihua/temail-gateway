package com.syswin.temail.cdtpserver.notify;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.entity.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;

/**
 * @author 姚华成
 * @date 2018/8/7
 */
@Slf4j
public class TemailServerMqListener implements MessageListenerConcurrently {
    private Gson gson = new Gson();
    
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt msg : msgs) {
                         
                String msgData = new String(msg.getBody());
                System.out.println("接收到的消息是：" + msgData);                
                TransferCDTPPackage  transferCDTPPackageJson = gson.fromJson(msgData, TransferCDTPPackage.class);//把JSON字符串转为对象  
                String to = transferCDTPPackageJson.getTo();   
                RespMsgHandler.sendMsg(transferCDTPPackageJson);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("队列传输出错！请求参数：" + msgs, e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private String getTopicByTemail(String temail) {
        // 根据temail地址从状态服务器获取该temail对应的通道所在topic
        return "defaultChannel";
    }
}
