package com.syswin.temail.ps.common.entity;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.ps.common.entity.CommandType.PING;
import static com.syswin.temail.ps.common.entity.CommandType.PONG;

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

  public boolean isHearbeat() {
    return commandSpace == CHANNEL.getCode() &&
        (command == PING.getCode() || command == PONG.getCode());
  }
}
