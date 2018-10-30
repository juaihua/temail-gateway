package com.syswin.temail.gateway.codec;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static com.syswin.temail.ps.common.utils.PacketUtil.unpack;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author 姚华成
 * @date 2018-10-30
 */
public class CommandAwarePacketUtil {

  public static byte[] decodeData(CDTPPacketTrans packet, boolean original) {
    String data = packet.getData();
    if (data == null) {
      return new byte[0];
    }
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (isSendSingleMsg(commandSpace, command) ||
        isSendGroupMsg(commandSpace, command)) {
      byte[] packetBytes = Base64.getUrlDecoder().decode(data);
      if (original) {
        CDTPPacket originalPacket = unpack(packetBytes);
        return originalPacket.getData();
      } else {
        return packetBytes;
      }
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }

  public static String encodeData(CDTPPacket packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (isSendSingleMsg(commandSpace, command) ||
        isSendGroupMsg(commandSpace, command)) {
      return Base64.getUrlEncoder().encodeToString(packet.getData());
    } else {
      return new String(packet.getData(), StandardCharsets.UTF_8);
    }
  }

  public static boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == SINGLE_MESSAGE_CODE && command == 1;
  }

  public static boolean isSendGroupMsg(short commandSpace, short command) {
    return commandSpace == GROUP_MESSAGE_CODE && command == 1;
  }

  public static boolean isGroupJoin(short commandSpace, short command) {
    return commandSpace == 2 && command == 0x0107;
  }

}
