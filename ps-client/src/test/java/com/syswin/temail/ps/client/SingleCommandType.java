package com.syswin.temail.ps.client;

import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-8-28
 */
public enum SingleCommandType {
  SEND_MESSAGE(1),
  SYNC_MESSAGE_LIST(2),
  SYNC_MESSAGE_DETAIL(3),
  DELETE_MESSAGE(4),
  REVOKE_MESSAGE(5),
  BURN_MESSAGE(6);
  @Getter
  private short code;

  SingleCommandType(int code) {
    this.code = (short) code;
  }
}
