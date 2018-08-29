package com.syswin.temail.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CDTPPacket {

  private short commandSpace;
  private short command;
  private short version;
  private CDTPHeader header;
  private byte[] data;

}
