package com.syswin.temail.gateway.exception;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
public class PacketException extends RuntimeException {

  public PacketException() {
  }

  public PacketException(String message) {
    super(message);
  }

  public PacketException(String message, Throwable cause) {
    super(message, cause);
  }

  public PacketException(Throwable cause) {
    super(cause);
  }
}
