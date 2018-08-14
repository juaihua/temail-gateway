package com.syswin.temail.cdtpserver.entity;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-13
 */
@Data
public class AuthData {

  private String temail;
  private String unsignedBytes;
  private String signature;

}
