package com.syswin.temail.cdtpserver.utils;

import java.nio.charset.Charset;

import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;

public class CdtpPackageUtil {

  public static void copyBeanProperties(TransferCDTPPackage transferCDTPPackage,
      CDTPPackage.Builder builder) {
    if ((null != transferCDTPPackage) && (builder != null)) {
      builder.setCommand(transferCDTPPackage.getCommand());
      builder.setVersion(transferCDTPPackage.getVersion());
      builder.setAlgorithm(transferCDTPPackage.getAlgorithm());
      if (null != transferCDTPPackage.getSign()) {
        builder.setSign(transferCDTPPackage.getSign());
      }

      builder.setDem(transferCDTPPackage.getDem());
      builder.setTimestamp(transferCDTPPackage.getTimestamp());
      if (null != transferCDTPPackage.getPkgId()) {
        builder.setPkgId(transferCDTPPackage.getPkgId());
      }

      if (null != transferCDTPPackage.getFrom()) {
        builder.setFrom(transferCDTPPackage.getFrom());
      }
      if (null != transferCDTPPackage.getTo()) {
        builder.setTo(transferCDTPPackage.getTo());
      }

      if (null != transferCDTPPackage.getSenderPK()) {
        builder.setSenderPK(transferCDTPPackage.getSenderPK());
      }

      if (null != transferCDTPPackage.getReceiverPK()) {
        builder.setReceiverPK(transferCDTPPackage.getReceiverPK());
      }
      builder.setData(ByteString.copyFrom(transferCDTPPackage.getData(), Charset.defaultCharset()));
    }
  }
  
  
 
  
}
