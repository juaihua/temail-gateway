package com.syswin.temail.cdtpserver.exception;

public class CdtpServerException extends RuntimeException {

  public CdtpServerException(String message, Throwable e) {
    super(message, e);
  }

  public CdtpServerException(String message) {
    super(message);
  }
}
