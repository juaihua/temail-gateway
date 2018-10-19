package com.syswin.temail.gateway.codec;

import com.syswin.temail.gateway.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PacketDecode {


  public static boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == CommandSpaceType.SINGLE_MESSAGE_CODE && command == 1;
  }

  public static boolean isSendGroupMsg(short commandSpace, short command) {
    return commandSpace == CommandSpaceType.GROUP_MESSAGE_CODE && command == 1;
  }

  public static byte[] decodeData(CDTPPacketTrans packet) {
    String data = packet.getData();
    if (data == null) {
      return new byte[0];
    }
    if (isSendSingleMsg(packet.getCommandSpace(), packet.getCommand())) {
      return Base64.getUrlDecoder().decode(data);
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }

  public static String encodeData(CDTPPacket packet) {
    if (isSendSingleMsg(packet.getCommandSpace(), packet.getCommand())) {
      return Base64.getUrlEncoder().encodeToString(packet.getData());
    } else {
      return new String(packet.getData(), StandardCharsets.UTF_8);
    }
  }

}
