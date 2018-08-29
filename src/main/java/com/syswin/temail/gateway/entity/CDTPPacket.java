package com.syswin.temail.gateway.entity;

import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPHeader;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPHeader.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CDTPPacket {

  private short commandSpace;
  private short command;
  private short version;
  private Header header;
  // TODO(姚华成) 字节数组转换成json对象的性能需要优化
  private byte[] data;
//  private PacketDataType dataType;
//  private String data;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static final class Header {

    private String deviceId;
    private int signatureAlgorithm;
    private String signature;
    private int dataEncryptionMethod;
    private long timestamp;
    private String packetId;
    private String sender;
    private String senderPK;
    private String receiver;
    private String receiverPK;
    private String at;
    private String topic;
    private String extraData;

    public static Header copyFrom(CDTPHeader cdtpHeader) {
      Header header = new Header();
      // TODO(姚华成) 试试BeanUtils
      // TODO: 2018/8/29 null for empty string from protobuf?
      header.setDeviceId(nullable(cdtpHeader.getDeviceId()));
      header.setSignatureAlgorithm(cdtpHeader.getSignatureAlgorithm());
      header.setSignature(nullable(cdtpHeader.getSignature()));
      header.setDataEncryptionMethod(cdtpHeader.getDataEncryptionMethod());
      header.setTimestamp(cdtpHeader.getTimestamp());
      header.setPacketId(nullable(cdtpHeader.getPacketId()));
      header.setSender(nullable(cdtpHeader.getSender()));
      header.setSenderPK(nullable(cdtpHeader.getSenderPK()));
      header.setReceiver(nullable(cdtpHeader.getReceiver()));
      header.setReceiverPK(nullable(cdtpHeader.getReceiverPK()));
      header.setAt(nullable(cdtpHeader.getAt()));
      header.setTopic(nullable(cdtpHeader.getTopic()));
      header.setExtraData(nullable(cdtpHeader.getExtraData()));
      return header;
    }

    private static String nullable(String value) {
      return value.isEmpty()? null : value;
    }

    public CDTPHeader toCDTPHeader() {
      Builder builder = CDTPHeader.newBuilder();
      if (deviceId != null) {
        builder.setDeviceId(getDeviceId());
      }
      builder.setSignatureAlgorithm(getSignatureAlgorithm());
      if (signature != null) {
        builder.setSignature(getSignature());
      }
      builder.setDataEncryptionMethod(getDataEncryptionMethod());
      builder.setTimestamp(getTimestamp());
      if (packetId != null) {
        builder.setPacketId(getPacketId());
      }
      if (sender != null) {
        builder.setSender(getSender());
      }
      if (senderPK != null) {
        builder.setSenderPK(getSenderPK());
      }
      if (receiver != null) {
        builder.setReceiver(getReceiver());
      }
      if (receiverPK != null) {
        builder.setReceiverPK(getReceiverPK());
      }
      if (at != null) {
        builder.setAt(getAt());
      }
      if (topic != null) {
        builder.setTopic(getTopic());
      }
      if (extraData != null) {
        builder.setExtraData(getExtraData());
      }
      return builder.build();
    }
  }

}
