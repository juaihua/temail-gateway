package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;

import com.syswin.temail.ps.client.utils.StringUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Map;
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
  // TODO 自动登录，会话管理

  private static final int DEFAULT_PORT = 8099;
  private Map<HostAndPort, CDTPClient> cdtpClientMap = new ConcurrentHashMap<>();
  private String deviceId;
  private String defaultHost;
  private int port;

  PsClientImpl(String deviceId, String defaultHost, int port) {
    this.deviceId = deviceId;
    this.defaultHost = defaultHost;
    this.port = port;
  }

  @Override
  public Message sendMessage(Message message) {
    CDTPClient cdtpClient = getCdtpClient(message);

    CDTPPacket packet = toCDTPPacket(message);
    signature(packet);
    CDTPPacket respPacket = cdtpClient.syncExecute(packet);
    return fromCDTPPacket(respPacket);
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
      if (port > 0) {
        hostAndPort.setPort(port);
      } else {
        hostAndPort.setPort(DEFAULT_PORT);
      }
    }
    CDTPClient cdtpClient = cdtpClientMap.computeIfAbsent(hostAndPort, key -> {
      CDTPClient value = new CDTPClient(key.getHost(), key.getPort());
      value.connect();
      return value;
    });
    String sender = message.getHeader().getSender();
    // 检查本地是否存在会话，如果没有本地会话，则登录
    login(sender);
    return cdtpClient;
  }

  private void login(String temail) {

  }

  @Override
  public void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Message> errorConsumer) {
    throw new UnsupportedOperationException("请不要着急，异常调用方法还未实现！");
  }

  private void signature(CDTPPacket packet) {
    // TODO(姚华成) 签名待实现
  }

  private Message fromCDTPPacket(CDTPPacket packet) {
    CDTPHeader cdtpHeader = packet.getHeader();
    Header header = new Header();
    header.setCommandSpace(packet.getCommandSpace());
    header.setCommand(packet.getCommand());
    header.setDataEncryptionMethod(cdtpHeader.getDataEncryptionMethod());
    header.setTimestamp(cdtpHeader.getTimestamp());
    header.setPacketId(cdtpHeader.getPacketId());
    header.setSender(cdtpHeader.getSender());
    header.setSenderPK(cdtpHeader.getSenderPK());
    header.setReceiver(cdtpHeader.getReceiver());
    header.setReceiverPK(cdtpHeader.getReceiverPK());
    header.setAt(cdtpHeader.getAt());
    header.setTopic(cdtpHeader.getTopic());
    header.setExtraData(cdtpHeader.getExtraData());
    header.setTargetAddress(cdtpHeader.getTargetAddress());

    Message message = new Message();
    message.setHeader(header);
    message.setPayload(packet.getData());
    return message;
  }

  private CDTPPacket toCDTPPacket(Message message) {
    Header header = message.getHeader();
    CDTPHeader cdtpHeader = new CDTPHeader();
    cdtpHeader.setDeviceId(deviceId);
    cdtpHeader.setDataEncryptionMethod(header.getDataEncryptionMethod());
    cdtpHeader.setTimestamp(header.getTimestamp());
    cdtpHeader.setPacketId(header.getPacketId());
    cdtpHeader.setSender(header.getSender());
    cdtpHeader.setSenderPK(header.getSenderPK());
    cdtpHeader.setReceiver(header.getReceiver());
    cdtpHeader.setReceiverPK(header.getReceiverPK());
    cdtpHeader.setAt(header.getAt());
    cdtpHeader.setTopic(header.getTopic());
    cdtpHeader.setExtraData(header.getExtraData());
    cdtpHeader.setTargetAddress(header.getTargetAddress());
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(header.getCommandSpace());
    packet.setCommand(header.getCommand());
    packet.setVersion(CDTP_VERSION);
    packet.setHeader(cdtpHeader);
    packet.setData(message.getPayload());
    return packet;
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
}
