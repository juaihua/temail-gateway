package com.syswin.temail.cdtpserver.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-8
 */
@Data
public class TransferCDTPPackage implements Serializable {
  private int command;
  private int version;
  private int algorithm;
  private String sign;
  private int dem;
  private long timestamp;
  private String pkgId;
  private String from;
  private String to;
  private String senderPK;
  private String receiverPK;
  private String data;
}
