package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-15
 */
public class PsClientException extends RuntimeException {

  public PsClientException(String message) {
    super(message);
  }

  public PsClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
