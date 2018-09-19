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

  public static final short SEND_MESSAGE_CODE = SEND_MESSAGE.code;
  public static final short SYNC_MESSAGE_LIST_CODE = SYNC_MESSAGE_LIST.code;
  public static final short SYNC_MESSAGE_DETAIL_CODE = SYNC_MESSAGE_DETAIL.code;
  public static final short DELETE_MESSAGE_CODE = DELETE_MESSAGE.code;
  public static final short REVOKE_MESSAGE_CODE = REVOKE_MESSAGE.code;
  public static final short BURN_MESSAGE_CODE = BURN_MESSAGE.code;

  @Getter
  private short code;

  SingleCommandType(int code) {
    this.code = (short) code;
  }
}
