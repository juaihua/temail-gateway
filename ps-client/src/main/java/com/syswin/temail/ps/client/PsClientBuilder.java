package com.syswin.temail.ps.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
@RequiredArgsConstructor
public class PsClientBuilder {

  private static final int DEFAULT_WRITE_IDLE_TIME_SECONDS = 30;
  private static final int DEFAULT_PORT = 8099;
  @NonNull
  private String deviceId;
  private String defaultHost = "127.0.0.1";
  private int defaultPort = DEFAULT_PORT;
  private int writeIdleTimeSeconds = DEFAULT_WRITE_IDLE_TIME_SECONDS;

  public PsClient build() {
    return new PsClientImpl(deviceId, defaultHost, defaultPort, writeIdleTimeSeconds);
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
}
