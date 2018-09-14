package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public interface PsClient {
  void connect();
  void disconnect();
  boolean login(String temail);
  boolean logout(String temail);
}
