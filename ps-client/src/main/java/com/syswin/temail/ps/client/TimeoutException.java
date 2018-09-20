package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
public class TimeoutException extends PsClientException {

  public TimeoutException(String message) {
    super(message);
  }

  public TimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
