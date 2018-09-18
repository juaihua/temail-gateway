package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.kms.vault.CipherAlgorithm;
import com.syswin.temail.kms.vault.KeyAwareAsymmetricCipher;
import com.syswin.temail.kms.vault.VaultKeeper;
import com.syswin.temail.ps.client.utils.StringUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
@Slf4j
class PsClientImpl implements PsClient {

  private Map<HostAndPort, CDTPClientEntity> cdtpClientMap = new ConcurrentHashMap<>();
  private String deviceId;
  private String defaultHost;
  private int port;
  private int writeIdleTimeSeconds;
  private int maxRetryInternal;
  private KeyAwareAsymmetricCipher cipher = new VaultKeeper().asymmetricCipher(CipherAlgorithm.SM2);

  PsClientImpl(String deviceId, String defaultHost, int port, int writeIdleTimeSeconds, int maxRetryInternal) {
    this.deviceId = deviceId;
    this.defaultHost = defaultHost;
    this.port = port;
    this.writeIdleTimeSeconds = writeIdleTimeSeconds;
    this.maxRetryInternal = maxRetryInternal;
  }

  @Override
  public Message sendMessage(Message message) {
    CDTPClient cdtpClient = getCdtpClient(message);

    CDTPPacket packet = getCdtpPacket(message);

    CDTPPacket respPacket = cdtpClient.getBlockingStub().execute(packet);
    return MessageConverter.fromCDTPPacket(respPacket);
  }

  @Override
  public void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Throwable> errorConsumer) {
    CDTPClient cdtpClient = getCdtpClient(message);

    CDTPPacket reqPacket = getCdtpPacket(message);

    cdtpClient.getAsyncStub().execute(reqPacket,
        packet -> responseConsumer.accept(MessageConverter.fromCDTPPacket(packet)),
        errorConsumer);
  }

  private CDTPClient getCdtpClient(Message message) {
    String targetAddress = message.getHeader().getTargetAddress();
    if (StringUtil.isEmpty(targetAddress)) {
      targetAddress = defaultHost;
    }
    HostAndPort hostAndPort = parseHostAndPort(targetAddress);
    if (hostAndPort.getHost() == null) {
      hostAndPort.setHost(defaultHost);
    }
    if (hostAndPort.getPort() == 0) {
      hostAndPort.setPort(port);
    }
    CDTPClientEntity cdtpClientEntity = cdtpClientMap.computeIfAbsent(hostAndPort,
        key -> {
          CDTPClient cdtpClient = new CDTPClient(key.getHost(), key.getPort(), writeIdleTimeSeconds, maxRetryInternal);
          cdtpClient.connect();
          return new CDTPClientEntity(cdtpClient);
        });
    String sender = message.getHeader().getSender();
    // 检查本地是否存在会话，如果没有本地会话，则登录
    if (!isLogged(sender, cdtpClientEntity)) {
      login(sender, cdtpClientEntity);
    }

    return cdtpClientEntity.getCdtpClient();
  }

  private CDTPPacket getCdtpPacket(Message message) {
    CDTPPacket packet = MessageConverter.toCDTPPacket(message);
    packet.getHeader().setDeviceId(deviceId);
    genSignature(packet);
    return packet;
  }

  private CDTPPacket genLoginPacket(String temail) {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(LOGIN.getCode());
    packet.setVersion(CDTP_VERSION);
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId(deviceId);
    header.setSender(temail);
    cipher.publicKey(temail).ifPresent(key -> header.setSenderPK(key.toString()));
    header.setPacketId(UUID.randomUUID().toString());

    packet.setHeader(header);
    packet.setData(new byte[0]);
    return packet;
  }

  private boolean isLogged(String sender, CDTPClientEntity cdtpClientEntity) {
    return cdtpClientEntity.getLoggedTemails().contains(sender);
  }

  private void login(String temail, CDTPClientEntity cdtpClientEntity) {
    try {
      CDTPClient cdtpClient = cdtpClientEntity.getCdtpClient();
      CDTPPacket packet = genLoginPacket(temail);
      genSignature(packet);
      CDTPPacket respPacket = cdtpClient.getBlockingStub().execute(packet);
      CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(respPacket.getData());
      if (!loginSuccess(loginResp)) {
        // 登录失败的处理，暂时简单抛出异常
        throw new PsClientException(temail + "登录失败，状态码：" + loginResp.getCode() + "，错误描述：" + loginResp.getDesc());
      }
      cdtpClientEntity.getLoggedTemails().add(temail);
    } catch (InvalidProtocolBufferException e) {
      throw new PsClientException("登录失败", e);
    }
  }

  private boolean loginSuccess(CDTPLoginResp loginResp) {
    return loginResp.getCode() == 200;
  }

  private void genSignature(CDTPPacket packet) {
//    String temail = packet.getHeader().getSender();
//    byte[] unsigned = new byte[100];
//    String sign = Base64.getEncoder().encodeToString(cipher.sign(temail, null));

    // TODO(姚华成) 签名待实现
  }

  private HostAndPort parseHostAndPort(String targetAddress) {
    if (!StringUtil.hasText(targetAddress)) {
      return new HostAndPort(null, 0);
    }
    String[] strings = targetAddress.split(":");
    if (strings.length == 1) {
      return new HostAndPort(strings[0], 0);
    }
    return new HostAndPort(strings[0], Integer.parseInt(strings[1]));
  }

  @Data
  @AllArgsConstructor
  private class HostAndPort {

    private String host;
    private int port;
  }

  @Data
  private class CDTPClientEntity {

    private CDTPClient cdtpClient;
    private Set<String> loggedTemails = new HashSet<>();

    public CDTPClientEntity(CDTPClient cdtpClient) {
      this.cdtpClient = cdtpClient;
    }
  }
}
