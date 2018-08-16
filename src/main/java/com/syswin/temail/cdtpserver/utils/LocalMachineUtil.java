package com.syswin.temail.cdtpserver.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalMachineUtil {


  public static String getLocalIp() {
    String localIp = "";
    try {
      InetAddress addr = InetAddress.getLocalHost();
      String ip = addr.getHostAddress().toString(); // 获取本机ip
    } catch (Exception ex) {
      log.error("获取本机IP失败", ex);
    }
    return localIp;
  }

  public static String getLocalProccesId() {
      String localProcessInf = ManagementFactory.getRuntimeMXBean().getName();
      String localPId = localProcessInf.split("@")[0];
      return localPId;
  }

}
