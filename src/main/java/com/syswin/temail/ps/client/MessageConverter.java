package com.syswin.temail.ps.client;

import com.syswin.temail.ps.common.Constants;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;

class MessageConverter {

  static Message fromCDTPPacket(CDTPPacket packet) {
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

  static CDTPPacket toCDTPPacket(Message message) {
    Header header = message.getHeader();
    CDTPHeader cdtpHeader = new CDTPHeader();
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
    packet.setVersion(Constants.CDTP_VERSION);
    packet.setHeader(cdtpHeader);
    packet.setData(message.getPayload());
    return packet;
  }
}