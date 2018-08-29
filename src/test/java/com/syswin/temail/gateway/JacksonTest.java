package com.syswin.temail.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.syswin.temail.gateway.client.PacketMaker;
import com.syswin.temail.gateway.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
public class JacksonTest {


  public static void main(String[] args) throws JsonProcessingException {
    new CDTPPacket();
    CDTPPacket packet = PacketMaker.loginPacket("sender", "receiver");
    ObjectMapper mapper = new ObjectMapper();
    System.out.println("原始值：" + packet);
    System.out.println("Jackson转发结果:" + mapper.writeValueAsString(packet));
    System.out.println("   Gson转换结果:" + new GsonBuilder().serializeNulls().create().toJson(packet));
//    encoder.encode(` `)

  }

}
