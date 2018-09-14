package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public class PsClientBuilder {

  private String host = "127.0.0.1";
  private int port = 8099;

  public PsClientBuilder host(String host) {
    this.host = host;
    return this;
  }

  public PsClientBuilder port(int port) {
    this.port = port;
    return this;
  }

  public PsCDTPClient build() {
    return new PsCDTPClient(host, port);
  }
}
