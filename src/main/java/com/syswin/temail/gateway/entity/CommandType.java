package com.syswin.temail.gateway.entity;

import com.syswin.temail.gateway.exception.PacketException;
import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Getter
public enum CommandType {
  // TODO(姚华成) 具体内容需要再定义
  PING(1),
  PONG(2),
  LOGIN(101),
  LOGOUT(102),

  INTERNAL_ERROR(600);

  private short code;

  CommandType(int code) {
    this.code = (short) code;
  }

  public static CommandType valueOf(short code) {
    CommandType.valueOf("");
    for (CommandType commandType : CommandType.values()) {
      if (commandType.getCode() == code) {
        return commandType;
      }
    }
    throw new PacketException("不支持的Command的编码：" + code);
  }

}
