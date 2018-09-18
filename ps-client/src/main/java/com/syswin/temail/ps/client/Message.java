package com.syswin.temail.ps.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

  private Header header;
  private byte[] payload;
}
