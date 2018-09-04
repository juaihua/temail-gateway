package com.syswin.temail.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
public class JacksonTest {


  public static void main(String[] args) throws JsonProcessingException {
//    int i = 131338;
//    System.out.println(Integer.toHexString(i));
//    new CDTPPacket();
//    CDTPPacket packet = PacketMaker.loginPacket("sender", "receiver");
//    ObjectMapper mapper = new ObjectMapper();
//    System.out.println("原始值：" + packet);
//    System.out.println("Jackson转发结果:" + mapper.writeValueAsString(packet));
//    System.out.println("   Gson转换结果:" + new GsonBuilder().serializeNulls().create().toJson(packet));
//    encoder.encode(` `)

    Gson gson = new Gson();

    JsonType type = new JsonType((byte) 1, (short) 2, 3, System.currentTimeMillis(), 1.0f, 2.0);
    MessageTypeBody messageTypeBody = new MessageTypeBody();
    messageTypeBody.setData(type);

    System.out.println("原始对象：" + messageTypeBody);
    String json = gson.toJson(messageTypeBody);
    System.out.println("原始转换：" + json);
    MessageBody map = gson.fromJson(json, MessageBody.class);
    System.out.println("Map对象：" + map);
    String json1 = gson.toJson(map);
    System.out.println("Map对象转换成json:" + json1);
    MessageTypeBody type1 = gson.fromJson(json1, MessageTypeBody.class);
    System.out.println("map->type:" + type1);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JsonType {

    private byte b;
    private short s;
    private int i;
    private long l;
    private float f;
    private double d;
//    private Date date;
  }
  @Data
  public static class MessageBody {

    private String receiver;
    private String header;
    private String data;
  }
  @Data
  public static class MessageTypeBody {

    private String receiver;
    private String header;
    private JsonType data;
  }

}
