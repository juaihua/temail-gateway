package com.syswin.temail.cdtpserver.notify;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.utils.CommandEnum;
import com.syswin.temail.cdtpserver.utils.SendMsg;

public class RespMsgHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());

  public static void sendMsg(TransferCDTPPackage transferCDTPPackage) {
    try {
      CDTPPackage.Builder builder = CDTPPackage.newBuilder();
      //BeanUtils.copyProperties(transferCDTPPackage, builder);
      copyBeanProperties(transferCDTPPackage, builder);
      CDTPPackage ctPackage = builder.build();

      String to = transferCDTPPackage.getTo();
      Map<String, TemailInfo> temailInfoMap = ActiveTemailManager.getAll(to);
      if(null != temailInfoMap  && !temailInfoMap.isEmpty()){
        Iterator iter = temailInfoMap.entrySet().iterator();
        while (iter.hasNext()) {
          Map.Entry<String, TemailInfo> entry = (Map.Entry<String, TemailInfo>) iter.next();
          String temail = (String) entry.getKey();
          TemailInfo temailInfo = (TemailInfo) entry.getValue();

          SendMsg.sendToTemail(ctPackage, temailInfo.getSocketChannel());
        }
      }
      else{
        LOGGER.info("no find temail  sockchannel in  ActiveTemailManager.");
      }
      
    } catch (Exception ex) {
      LOGGER.error("send tomail msg  error",  ex);
    }
  }
  
  public static  void   copyBeanProperties(TransferCDTPPackage transferCDTPPackage,  CDTPPackage.Builder  builder){

    builder.setCommand(transferCDTPPackage.getCommand());
    builder.setVersion(transferCDTPPackage.getVersion());
    builder.setAlgorithm(transferCDTPPackage.getAlgorithm());
    if(null != transferCDTPPackage.getSign()){
      builder.setSign(transferCDTPPackage.getSign());
    }
    
    builder.setDem(transferCDTPPackage.getDem());
    builder.setTimestamp(transferCDTPPackage.getTimestamp());
    if(null != transferCDTPPackage.getPkgId()){
      builder.setPkgId(transferCDTPPackage.getPkgId());
    }
    
    if(null != transferCDTPPackage.getFrom()){
      builder.setFrom(transferCDTPPackage.getFrom());
    }
    if(null != transferCDTPPackage.getTo()){
      builder.setTo(transferCDTPPackage.getTo());
    }
    
    if(null != transferCDTPPackage.getSenderPK()){
      builder.setSenderPK(transferCDTPPackage.getSenderPK());
    }
    
    if(null !=  transferCDTPPackage.getReceiverPK()){
      builder.setReceiverPK(transferCDTPPackage.getReceiverPK());
    }
    
    
    builder.setData(ByteString.copyFrom(transferCDTPPackage.getData(), Charset.defaultCharset())); 
  }
  
}