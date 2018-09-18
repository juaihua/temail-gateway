package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public class PsClientBuilder {

  private static final int DEFAULT_WRITE_IDLE_TIME_SECONDS = 30;
  private static final int DEFAULT_PORT = 8099;
  private String deviceId;
  private String defaultHost = "127.0.0.1";
  private int defaultPort = DEFAULT_PORT;
  private int writeIdleTimeSeconds = DEFAULT_WRITE_IDLE_TIME_SECONDS;
  private int maxRetryInternal = 1;

  public PsClientBuilder(String deviceId) {
    this.deviceId = deviceId;
  }

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
