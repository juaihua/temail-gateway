package com.syswin.temail.ps.client;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author 姚华成
 * @date 2018-9-17
 */
@Slf4j
public class PsClientTest {

  private static Gson gson = new Gson();
  private String sender = "jack@throwable.email";
  private String receive = "sean@throwable.email";
  private String deviceId = "deviceId";
  private String content = "hello world";

  // 创建单聊消息体
  public static Message singleChatPacket(String sender, String recipient, String content) {
    Header header = new Header();

    header.setCommandSpace(CommandSpaceType.SINGLE_MESSAGE_CODE);
    header.setCommand(SingleCommandType.SEND_MESSAGE.getCode());

    header.setDataEncryptionMethod(0);
    header.setTimestamp(1535713173935L);
    header.setPacketId(UUID.randomUUID().toString());
    header.setSender(sender);
    header.setReceiver(recipient);
    header.setSenderPK("SenderPK123");
    header.setReceiverPK("ReceiverPK456");
    Map<String, Object> extraData = new HashMap<>();
    extraData.put("from", sender);
    extraData.put("to", recipient);
    extraData.put("storeType", "2");
    extraData.put("type", "0");
    extraData.put("msgId", "4298F38F87DC4775B264A3753E77B443");
    header.setExtraData(gson.toJson(extraData));
    header.setTargetAddress("192.168.1.194");

    return new Message(header, content.getBytes());
  }

  @Test
  public void sendMessage() {
    PsClientBuilder builder = new PsClientBuilder(deviceId);
    PsClient psClient = builder.build();
    psClient.sendMessage(singleChatPacket(sender, receive, content));
  }

  @Test
  public void sendMessageAsync() throws InterruptedException {
    PsClientBuilder builder = new PsClientBuilder(deviceId);
    PsClient psClient = builder.build();
    CountDownLatch latch = new CountDownLatch(1);
    psClient.sendMessage(singleChatPacket(sender, receive, content), message -> {
      log.info(message.toString());
      latch.countDown();
    }, t -> {
      log.error("调用错误", t);
      latch.countDown();
    });
    latch.await(10, TimeUnit.SECONDS);
  }
}