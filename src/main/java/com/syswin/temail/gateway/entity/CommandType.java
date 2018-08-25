package com.syswin.temail.gateway.entity;

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
  LOGIN(100),
  LOGOUT(101),
  BIZ(1000);
  private short command;

  CommandType(int command) {
    this.command = (short) command;
  }

  public static CommandType valueOf(short command) {
    CommandType.valueOf("");
    for (CommandType commandType : CommandType.values()) {
      if (commandType.getCommand() == command) {
        return commandType;
      }
    }
    return BIZ;
  }

}
