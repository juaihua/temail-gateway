package com.syswin.temail.ps.client;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import org.junit.Test;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public class PsCDTPClientTest {

  @Test
  public void test() {
    PsClientBuilder builder = new PsClientBuilder();
    PsCDTPClient client = builder.port(9999).build();
    client.connect();

    CDTPPacket packet = new CDTPPacket();
    CDTPHeader header = new CDTPHeader();
    header.setSender("sender");
    header.setReceiver("receiver");
    header.setDeviceId("deviceId");
    packet.setHeader(header);
    packet.setCommand((short) 1);
    packet.setCommandSpace((short) 0);
    packet.setVersion((short) 1);
    packet.setData("TestData".getBytes());
    CDTPPacket respPacket = client.syncExecute(packet);
    System.out.println(respPacket);
  }
}