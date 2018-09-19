package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.client.SingleCommandType.SEND_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGOUT_CODE;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
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
    header.setSender(sender);
    header.setReceiver(receiver);
    header.setPacketId(UUID.randomUUID().toString());
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
