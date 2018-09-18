package com.syswin.temail.ps.client;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * @author 姚华成
 * @date 2018-9-17
 */
public class PsClientTest {

  private static Gson gson = new Gson();
  private String sender = "jack@t.email";
  private String receive = "sean@t.email";
  private String deviceId = "deviceId";
  private String content = "hello world";

  // 创建单聊消息体
  public static Message singleChatPacket(String sender, String recipient, String content) {
    Header header = new Header();

    header.setCommandSpace(CommandSpaceType.SINGLE_MESSAGE.getCode());
    header.setCommand(SingleCommandType.SEND_MESSAGE.getCode());

    header.setDataEncryptionMethod(0);
    header.setTimestamp(1535713173935L);
    header.setPacketId("pkgId");
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

    return new Message(header, content.getBytes());
  }

  @Test
  public void sendMessage() {
    PsClientBuilder builder = new PsClientBuilder(deviceId);
    PsClient psClient = builder.build();
    psClient.sendMessage(singleChatPacket(sender, receive, content));
  }

  @Test
  public void sendMessageAsync() {
  }
}