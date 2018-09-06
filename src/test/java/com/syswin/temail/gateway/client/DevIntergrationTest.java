package com.syswin.temail.gateway.client;

import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.gateway.entity.CommandSpaceType.SINGLE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.TemailGatewayApplication;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.gateway.entity.Response;
import groovy.util.logging.Slf4j;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest(
    classes = TemailGatewayApplication.class,
    properties = {
        "temail.gateway.verifyUrl=http://localhost:8090/verify",
        "temail.gateway.dispatchUrl=http://localhost:8090/dispatch",
        "temail.gateway.updateSocketStatusUrl=http://localhost:8090/updateStatus",
        "temail.gateway.mqTopic=temail-gateway",
        "temail.gateway.allIdleTimeSeconds=3"
    })
@RunWith(SpringRunner.class)
public class DevIntergrationTest {

  private static Gson gson = new Gson();

  private String sender = "jack@t.email";
  private String receive = "sean@t.email";
  private String deviceId = "deviceId";
  private String message = "hello world";
  private YHCNettyClient client;
  @Resource
  private TemailGatewayProperties properties;

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
