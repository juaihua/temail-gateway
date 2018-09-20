package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.client.SingleCommandType.SEND_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGOUT_CODE;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author 姚华成
 * @date 2018-9-19
 */
class PacketMaker {

  private static Gson gson = new Gson();

  static CDTPPacket loginPacket(String temail) {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(LOGIN_CODE);
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId("deviceId");
    header.setSender(temail);
    header.setPacketId(UUID.randomUUID().toString());
    packet.setHeader(header);
    packet.setData("".getBytes());
    return packet;
  }

  static CDTPPacket logoutPacket(String temail) {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(LOGOUT_CODE);
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId("deviceId");
    header.setSender(temail);
    header.setPacketId(UUID.randomUUID().toString());
    packet.setHeader(header);
    packet.setData("".getBytes());
    return packet;
  }


  static CDTPPacket sendSingleCharPacket(String sender, String receiver, String content) {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(SINGLE_MESSAGE_CODE);
    packet.setCommand(SEND_MESSAGE_CODE);
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId("deviceId");
    header.setDataEncryptionMethod(0);
    header.setTimestamp(System.currentTimeMillis());
    header.setPacketId(UUID.randomUUID().toString());

    header.setSender(sender);
    header.setReceiver(receiver);
    Map<String, Object> extraData = new HashMap<>();
    extraData.put("from", sender);
    extraData.put("to", receiver);
    extraData.put("storeType", "2");
    extraData.put("type", "0");
    extraData.put("msgId", "4298F38F87DC4775B264A3753E77B443");
    header.setExtraData(gson.toJson(extraData));
//    header.setTargetAddress("192.168.1.194");

    packet.setHeader(header);
    packet.setData(content.getBytes());
    return packet;
  }

  static CDTPPacket sendSingleCharRespPacket(CDTPPacket packet) {
    CDTPPacket respPacket = new CDTPPacket(packet);
    respPacket.setData(gson.toJson(HttpResponse.ok()).getBytes());
    return respPacket;
  }


}
