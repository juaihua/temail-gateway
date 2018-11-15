package com.syswin.temail.ps.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CDTPPacketTrans {

  private short commandSpace;
  private short command;
  private short version;
  private CDTPHeader header;
  private String data;

  public CDTPPacketTrans(CDTPPacket packet) {
    this(packet.getCommandSpace(),
        packet.getCommand(),
        packet.getVersion(),
        packet.getHeader().clone(),
        new String(packet.getData()));
  }
}
