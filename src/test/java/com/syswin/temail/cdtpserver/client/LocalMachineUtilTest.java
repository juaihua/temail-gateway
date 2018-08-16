package com.syswin.temail.cdtpserver.client;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

import com.syswin.temail.cdtpserver.entity.TemailSocketOptEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalMachineUtilTest {

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    System.out.println("TemailSocketOptEnum:" + TemailSocketOptEnum.add.toString());
    System.out.println("localIp:" + getLocalIp());
    System.out.println("LocalProccesId:" + getLocalProccesId());

  }


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
