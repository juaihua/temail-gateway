package com.syswin.temail.cdtpserver.client;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import java.util.HashMap;
import java.util.Map;

public class PackageMaker {

  private static final Gson gson = new Gson();

  // 创建单聊消息体
  static CDTPPacket singleChat(String sender, String recipient) {

    CDTPPacket packet = new CDTPPacket();
    packet.setCommand((short) 1000);
    packet.setVersion((short) 13);

    Header header = new Header();
    header.setSignatureAlgorithm(11);
    header.setSignature("sign");
    header.setDataEncryptionMethod(1);
    header.setTimestamp(System.currentTimeMillis());
    header.setPacketId("pkgId");
    header.setSender(sender);
    header.setReceiver(recipient);
    header.setSenderPK("SenderPK123");
    header.setReceiverPK("ReceiverPK456");
    packet.setHeader(header);

    Map<String, Map<String, Object>> cdtpBody = new HashMap<>();
    cdtpBody.put("header", new HashMap<>());
    cdtpBody.put("query", new HashMap<>());
    cdtpBody.put("body", new HashMap<>());

    Map<String, Object> body = cdtpBody.get("body");
    body.put("from", sender);
    body.put("fromMsg", "temail-gateway-str");
    body.put("msgid", "syswin-1534131915194-4");
    body.put("seqNo", 3);
    body.put("to", recipient);
    body.put("toMsg", "temail-gateway-str");
    body.put("type", 0);

    packet.setData(gson.toJson(cdtpBody).getBytes());

    return packet;
  }
}
