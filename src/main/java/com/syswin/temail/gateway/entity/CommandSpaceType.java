package com.syswin.temail.gateway.entity;

import com.syswin.temail.gateway.exception.PacketException;
import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Getter
public enum CommandSpaceType {
  CHANNEL(0),
  SINGLE_MESSAGE(1),
  GROUP_MESSAGE(2),
  STRATEGY(3);

  private short code;

  CommandSpaceType(int code) {
    this.code = (short) code;
  }

  public static CommandSpaceType valueOf(short code) {
    for (CommandSpaceType value : values()) {
      if (value.code == code) {
        return value;
      }
    }
    throw new PacketException("不支持的CommandSpace的编码：" + code);
  }
}
