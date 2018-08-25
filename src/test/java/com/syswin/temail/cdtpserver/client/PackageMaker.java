package com.syswin.temail.cdtpserver.client;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.CDTPBody;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class PackageMaker {

  // 创建单聊消息体
  static CDTPPackage singleChat(String sender, String recipient) {

    CDTPPackage.Builder builder = CDTPPackage.newBuilder();
    builder.setAlgorithm(11);
    builder.setCommand(1000);

    builder.setVersion(13);
    builder.setSign("sign");
    builder.setDem(1);
    builder.setTimestamp(System.currentTimeMillis());
    builder.setPkgId("pkgId");
    builder.setFrom(sender);
    builder.setTo(recipient);
    builder.setSenderPK("SenderPK123");
    builder.setReceiverPK("ReceiverPK456");

    CDTPBody cdtpBody = new CDTPBody();
    cdtpBody.setHeader(new HashMap<>());
    cdtpBody.setQuery(new HashMap<>());
    cdtpBody.setBody(new HashMap<>());

    Map<String, Object> body = cdtpBody.getBody();
    body.put("from", sender);
    body.put("fromMsg", "temail-gateway-str");
    body.put("msgid", "syswin-1534131915194-4");
    body.put("seqNo", 3);
    body.put("to", recipient);
    body.put("toMsg", "temail-gateway-str");
    body.put("type", 0);

    Gson gson = new Gson();
    String cdtpBodygsonString = gson.toJson(cdtpBody);

    builder.setData(ByteString.copyFrom(cdtpBodygsonString, Charset.defaultCharset()));
    CDTPPackage ctPackage = builder.build();

    return ctPackage;
  }
}
