package com.syswin.temail.ps.client;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HttpResponse<T> {

  private static final int OK = 200;

  private Integer code;
  private String message;
  private T data;

  private HttpResponse(Integer code) {
    this.code = code;
  }

  private HttpResponse(Integer code, String message) {
    this.code = code;
    this.message = message;
  }

  private HttpResponse(Integer code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public static <T> HttpResponse<T> ok() {
    return new HttpResponse<T>(OK);
  }

  public static <T> HttpResponse<T> ok(Integer code) {
    return ok(code, null);
  }

  public static <T> HttpResponse<T> ok(T body) {
    return ok(OK, body);
  }

  public static <T> HttpResponse<T> ok(Integer code, T body) {
    return new HttpResponse<>(code, null, body);
  }

  public static <T> HttpResponse<T> failed(Integer code) {
    return new HttpResponse<T>(code);
  }

  public static <T> HttpResponse<T> failed(Integer code, String message) {
    return new HttpResponse<T>(code, message);
  }

  public static <T> HttpResponse<T> failed(Integer code, String message, T body) {
    return new HttpResponse<>(code, message, body);
  }

}
