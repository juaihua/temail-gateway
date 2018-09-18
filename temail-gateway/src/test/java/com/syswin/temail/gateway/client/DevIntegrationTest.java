package com.syswin.temail.gateway.client;

import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class DevIntegrationTest {

  private String sender = "jack@t.email";
  private String receive = "sean@t.email";
  private String deviceId = "deviceId";
  private String message = "hello world";
  private YHCNettyClient client;

  @Before
  public void init() {
    if (client == null) {
      //client = new YHCNettyClient("127.0.0.1", 8099);
      client = new YHCNettyClient("192.168.1.194", 8099);
      client.start();
    }
  }



  @Test
  public void login() throws InvalidProtocolBufferException {
    CDTPPacket packet = loginPacket(sender, deviceId);
    CDTPPacket result = client.syncExecute(packet);
    CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(result.getData());
    assertThat(loginResp.getCode()).isEqualTo(200);
  }

  @Test
  public void singleChar() {

    CDTPPacket packet = loginPacket(sender, deviceId);
    client.syncExecute(packet);
    packet = singleChatPacket(sender, receive, message, deviceId);
    CDTPPacket result = client.syncExecute(packet);
    System.out.println(result);
  }

}
