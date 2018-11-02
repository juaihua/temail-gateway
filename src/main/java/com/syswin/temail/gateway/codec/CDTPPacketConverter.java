package com.syswin.temail.gateway.codec;

import static com.syswin.temail.gateway.codec.CommandAwarePacketUtil.decodeData;
import static com.syswin.temail.gateway.codec.CommandAwarePacketUtil.encodeData;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;

/**
 * @author 姚华成
 * @date 2018-10-30
 */
public class CDTPPacketConverter {

  public static CDTPPacketTrans toTrans(CDTPPacket packet) {
    if (packet == null) {
      return null;
    }
    return new CDTPPacketTrans(packet.getCommandSpace(), packet.getCommand(), packet.getVersion(),
        packet.getHeader().clone(), encodeData(packet));
  }

  public static CDTPPacket fromTrans(CDTPPacketTrans packetTrans) {
    if (packetTrans == null) {
      return null;
    }
    return new CDTPPacket(packetTrans.getCommandSpace(), packetTrans.getCommand(), packetTrans.getVersion(),
        packetTrans.getHeader().clone(),
        decodeData(packetTrans, false));
  }
}
