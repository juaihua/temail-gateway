package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.client.Constants.DEFAULT_EXECUTE_TIMEOUT;
import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.ps.client.utils.StringUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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

  private final Map<HostAndPort, CDTPClientEntity> cdtpClientMap = new ConcurrentHashMap<>();
  private final String deviceId;
  private final String defaultHost;
  private final int port;
  private final int idleTimeSeconds;
  private final Signer signer;

  PsClientImpl(String deviceId, String defaultHost, int port, int idleTimeSeconds, Signer signer) {
    this.deviceId = deviceId;
    this.defaultHost = defaultHost;
    this.port = port;
    this.idleTimeSeconds = idleTimeSeconds;
    this.signer = signer;
  }

  @Override
  public Message sendMessage(Message message) {
    return sendMessage(message, DEFAULT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
  }

  @Override
  public Message sendMessage(Message message, long timeout, TimeUnit timeUnit) {
    checkMessage(message);
    CDTPClient cdtpClient = getCdtpClient(message);
    CDTPPacket packet = getCdtpPacket(message);
    CDTPPacket respPacket = cdtpClient.syncExecute(packet, timeout, timeUnit);
    return MessageConverter.fromCDTPPacket(respPacket);
  }

  @Override
  public void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Throwable> errorConsumer) {
    sendMessage(message, responseConsumer, errorConsumer, DEFAULT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
  }

  @Override
  public void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Throwable> errorConsumer,
      long timeout, TimeUnit timeUnit) {
    checkMessage(message);
    CDTPClient cdtpClient = getCdtpClient(message);
    CDTPPacket reqPacket = getCdtpPacket(message);
    cdtpClient.asyncExecute(reqPacket,
        packet -> responseConsumer.accept(MessageConverter.fromCDTPPacket(packet)),
        errorConsumer, timeout, timeUnit);
  }

  private void checkMessage(Message message) {
    Header header = message.getHeader();
    if (header == null) {
      throw new PsClientException("header不允许为空！");
    }
    if (!StringUtil.hasText(header.getSender())) {
      throw new PsClientException("Sender不允许为空！");
    }
    if (!StringUtil.hasText(header.getSenderPK())) {
      throw new PsClientException("SenderPK不允许为空！");
    }
    if (!StringUtil.hasText(header.getPacketId())) {
      throw new PsClientException("PacketId不允许为空！");
    }

    if (message.getPayload() == null) {
      throw new PsClientException("payload不允许为空！");
    }
  }

  private CDTPClient getCdtpClient(Message message) {
    Header header = message.getHeader();
    String targetAddress = header.getTargetAddress();
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
          CDTPClient cdtpClient = new CDTPClient(key.getHost(), key.getPort(), idleTimeSeconds);
          cdtpClient.connect();
          return new CDTPClientEntity(cdtpClient);
        });
    CDTPClient cdtpClient = cdtpClientEntity.getCdtpClient();
    if (!cdtpClient.isActive()) {
      cdtpClient.realConnect();
    }
    String sender = header.getSender();
    // 检查本地是否存在会话，如果没有本地会话，则登录
    if (!isLogged(sender, cdtpClientEntity)) {
      login(sender, header.getSenderPK(), cdtpClientEntity);
    }

    return cdtpClient;
  }

  private CDTPPacket getCdtpPacket(Message message) {
    CDTPPacket packet = MessageConverter.toCDTPPacket(message);
    packet.getHeader().setDeviceId(deviceId);
    genSignature(packet);
    return packet;
  }

  public void genSignature(CDTPPacket packet) {
    try {
      CDTPHeader header = packet.getHeader();
      byte[] dataSha256 = MessageDigest.getInstance("SHA-256").digest(packet.getData());
      String unsigned =
          String.valueOf(packet.getCommandSpace() + packet.getCommand()) + header.getTargetAddress() + String
              .valueOf(header.getTimestamp()) + Base64.getEncoder().encodeToString(dataSha256);
      String temail = header.getSender();
      if (signer != null) {
        header.setSignatureAlgorithm(signer.getAlgorithm());
        header.setSignature(signer.sign(temail, unsigned));
      }
    } catch (NoSuchAlgorithmException e) {
      throw new PsClientException("对数据进行签名时出错！", e);
    }
  }

  private CDTPPacket genLoginPacket(String temail, String temailPK) {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(LOGIN.getCode());
    packet.setVersion(CDTP_VERSION);
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId(deviceId);
    header.setSender(temail);
    header.setSenderPK(temailPK);
    header.setPacketId(UUID.randomUUID().toString());

    packet.setHeader(header);
    packet.setData(new byte[0]);
    return packet;
  }

  private boolean isLogged(String sender, CDTPClientEntity cdtpClientEntity) {
    return cdtpClientEntity.getLoggedTemails().contains(sender);
  }

  private void login(String temail, String temailPK, CDTPClientEntity cdtpClientEntity) {
    try {
      CDTPClient cdtpClient = cdtpClientEntity.getCdtpClient();
      CDTPPacket packet = genLoginPacket(temail, temailPK);
      genSignature(packet);
      CDTPPacket respPacket = cdtpClient.syncExecute(packet);
      if (respPacket == null) {
        throw new TimeoutException(temail + "登录超时");
      }
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
