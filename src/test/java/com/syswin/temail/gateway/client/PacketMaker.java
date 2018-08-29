package com.syswin.temail.gateway.client;

import static com.syswin.temail.gateway.Constants.CDTP_VERSION;
import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPHeader;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLogin.Builder;
import com.syswin.temail.gateway.entity.CommandSpaceType;
import com.syswin.temail.gateway.entity.CommandType;
import java.util.HashMap;
import java.util.Map;

public class PacketMaker {

  private static final Gson gson = new Gson();

  // 创建单聊消息体
  public static CDTPPacket singleChatPacket(String sender, String recipient, String message, String deviceId) {

    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CommandSpaceType.SINGLE_MESSAGE.getCode());
    packet.setCommand(SingleCommandType.SEND_MESSAGE.getCode());
    packet.setVersion(CDTP_VERSION);

    CDTPHeader header = new CDTPHeader();
    header.setSignatureAlgorithm(1);
    header.setSignature("sign");
    header.setDataEncryptionMethod(0);
    header.setTimestamp(System.currentTimeMillis());
    header.setPacketId("pkgId");
    header.setDeviceId(deviceId);
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
    packet.setHeader(header);

    packet.setData(message.getBytes());

    return packet;
  }

  public static CDTPPacket loginPacket(String sender, String deviceId) {
    CDTPPacket packet = new CDTPPacket();
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId(deviceId);
    header.setSignatureAlgorithm(1);
    header.setTimestamp(System.currentTimeMillis());
    header.setDataEncryptionMethod(0);
    header.setPacketId("PacketId1234");
    header.setSender(sender);
    header.setSenderPK("SenderPK");
//    header.setReceiver("sean@t.email");
//    header.setReceiverPK("ReceiverPK");

    packet.setCommandSpace(CHANNEL.getCode());
    packet.setCommand(CommandType.LOGIN.getCode());
    packet.setVersion(CDTP_VERSION);
    packet.setHeader(header);

    Builder builder = CDTPLogin.newBuilder();

//    builder.setdevId("设备ID");
    builder.setPushToken("推送token");
    builder.setPlatform("ios/android/pc");
    builder.setOsVer("11.4");
    builder.setAppVer("1.0.0");
    builder.setLang("en、ch-zn...");
    builder.setTemail("请求发起方的temail地址");
    builder.setChl("渠道号");
    CDTPLogin cdtpLogin = builder.build();

    packet.setData(cdtpLogin.toByteArray());
    return packet;
  }

}
