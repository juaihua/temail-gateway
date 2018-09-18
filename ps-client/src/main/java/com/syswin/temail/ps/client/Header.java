package com.syswin.temail.ps.client;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
@Data
public class Header {

  private short commandSpace;
  private short command;
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
  private String targetAddress;

}
