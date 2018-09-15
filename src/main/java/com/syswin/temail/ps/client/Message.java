package com.syswin.temail.ps.client;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
@Data
public class Message {
  private Header header;
  private byte[] payload;
}
