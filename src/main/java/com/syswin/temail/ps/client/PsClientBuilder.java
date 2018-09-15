package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public class PsClientBuilder {

  private String deviceId;
  private String defaultHost;
  private int defaultPort;

  public PsClient build() {
    return new PsClientImpl(deviceId, defaultHost, defaultPort);
  }

  public PsClientBuilder deviceId(String deviceId) {
    this.deviceId = deviceId;
    return this;
  }

  public PsClientBuilder defaultHost(String defaultHost) {
    this.defaultHost = defaultHost;
    return this;
  }

  public PsClientBuilder defaultPort(int defaultPort) {
    this.defaultPort = defaultPort;
    return this;
  }
}
