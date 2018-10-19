package com.syswin.temail.gateway.entity;

import static com.syswin.temail.gateway.codec.PacketDecode.decodeData;
import static com.syswin.temail.gateway.codec.PacketDecode.encodeData;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
@Data
@AllArgsConstructor
public class CDTPPacketTrans {

  private short commandSpace;
  private short command;
  private short version;
  private CDTPHeader header;
  private String data;

  public CDTPPacketTrans() {
  }

  public CDTPPacketTrans(CDTPPacket packet) {
    this(packet.getCommandSpace(), packet.getCommand(), packet.getVersion(), packet.getHeader().clone(),
        encodeData(packet));
  }

  public CDTPPacket toCDTPPacket() {
    return new CDTPPacket(commandSpace, command, version, header.clone(),
        decodeData(this));
  }

}
