package com.syswin.temail.ps.client;

import com.syswin.temail.ps.client.utils.StringUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 用于构建PsClient对象的构造器。deviceId是必须的，其他参数可选。
 * <ul>
 * <li>deviceId：</li>
 * <li>defaultHost：默认的凤凰服务器的主机名。当请求的消息里没有提供targetAddress时，使用该默认主机名。没有提供时，默认值为本地主机名</li>
 * <li>defaultPort：targetAddress或者defaultHost中没有提供端口时，连接使用的默认端口号。没有提供时，默认是8099</li>
 * <li>idleTimeSeconds：通道没有发送消息时，为保持连接而发送心跳的空闲时间，以秒为单位。不能超过服务器的空闲时间，否则无法保持连接。默认为30秒</li>
 * </ul>
 *
 * @author 姚华成
 * @date 2018-9-14
 */
@RequiredArgsConstructor
public class PsClientBuilder {

  private static final int DEFAULT_IDLE_TIME_SECONDS = 30;
  private static final int DEFAULT_PORT = 8099;
  @NonNull
  private String deviceId;
  private String defaultHost = "127.0.0.1";
  private int defaultPort = DEFAULT_PORT;
  private int idleTimeSeconds = DEFAULT_IDLE_TIME_SECONDS;
  private Signer signer;
  private String vaultRegistryUrl;
  private String tenantId;

  /**
   * 构建PsClient对象
   *
   * @return 构造好的PsClient对象
   */
  public PsClient build() {
    assert StringUtil.hasText(deviceId);
    if (signer == null) {
      if (StringUtil.hasText(vaultRegistryUrl) && StringUtil.hasText(tenantId)) {
        signer = new KeyAwareSigner(vaultRegistryUrl, tenantId);
      }
    }
    return new PsClientImpl(deviceId, defaultHost, defaultPort, idleTimeSeconds, signer);
  }

  public PsClientBuilder defaultHost(String defaultHost) {
    this.defaultHost = defaultHost;
    return this;
  }

  public PsClientBuilder defaultPort(int defaultPort) {
    this.defaultPort = defaultPort;
    return this;
  }

  public PsClientBuilder idleTimeSeconds(int idleTimeSeconds) {
    this.idleTimeSeconds = idleTimeSeconds;
    return this;
  }

  public PsClientBuilder vaultRegistryUrl(String vaultRegistryUrl) {
    this.vaultRegistryUrl = vaultRegistryUrl;
    return this;
  }

  public PsClientBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public PsClientBuilder signer(Signer signer) {
    this.signer = signer;
    return this;
  }
}
