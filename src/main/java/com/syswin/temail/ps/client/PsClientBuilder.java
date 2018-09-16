package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public class PsClientBuilder {

  private static final int DEFAULT_WRITE_IDLE_TIME_SECONDS = 30;
  private String deviceId;
  private String defaultHost;
  private int defaultPort;
  private int writeIdleTimeSeconds = DEFAULT_WRITE_IDLE_TIME_SECONDS;
  private int maxRetryInternal;

  public PsClient build() {
    return new PsClientImpl(deviceId, defaultHost, defaultPort, writeIdleTimeSeconds, maxRetryInternal);
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

  public PsClientBuilder writeIdleTimeSeconds(int writeIdleTimeSeconds) {
    this.writeIdleTimeSeconds = writeIdleTimeSeconds;
    return this;
  }

  public PsClientBuilder maxRetryInternal(int maxRetryInternal) {
    this.maxRetryInternal = maxRetryInternal;
    return this;
  }
}
