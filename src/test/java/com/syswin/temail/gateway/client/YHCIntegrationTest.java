package com.syswin.temail.gateway.client;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.YHCNettyClient.responseHandler;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.TemailGatewayApplication;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.gateway.entity.Response;
import io.netty.channel.Channel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 姚华成
 * @date 2018-8-27
 */
//@Ignore
@Slf4j
@SpringBootTest(classes = TemailGatewayApplication.class,
    properties = {
        "temail.gateway.verifyUrl=http://localhost:8081/verify",
        "temail.gateway.dispatchUrl=http://localhost:8081/dispatch",
        "temail.gateway.updateSocketStatusUrl=http://localhost:8090/locations",
        "temail.gateway.mqTopic=temail-gateway",
        "temail.gateway.allIdleTimeSeconds=3"
    })
@RunWith(SpringRunner.class)
public class YHCIntegrationTest {

//  @ClassRule
//  public static final WireMockRule wireMockRule = new WireMockRule(8090);
  private static final Gson GSON = new Gson();
  private static final String ackMessage = uniquify("Sent");
  private static Channel channel;
  private final String sender = "jack@t.email";
  private final String receive = "sean@t.email";
  private final String deviceId = "deviceId";
  private final BlockingQueue<CDTPPacket> receivedPackages = new LinkedBlockingQueue<>();
  private final BlockingQueue<CDTPPacket> toBeSentPackages = new LinkedBlockingQueue<>();
  private final String message = "hello world";
  @Resource
  private TemailGatewayProperties properties;

  @BeforeClass
  public static void beforeClass() {
//    stubFor(post(urlEqualTo("/verify"))
//        .willReturn(
//            aResponse()
//                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
//                .withStatus(SC_OK)
//                .withBody(GSON.toJson(Response.ok())))
//    );
//
//    stubFor(post(urlEqualTo("/dispatch"))
//        .willReturn(
//            aResponse()
//                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
//                .withStatus(SC_OK)
//                .withBody(GSON.toJson(Response.ok(ackPayload())))));
//
//    stubFor(post(urlEqualTo("/updateStatus"))
//        .willReturn(
//            aResponse()
//                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
//                .withStatus(SC_OK)
//                .withBody(GSON.toJson(Response.ok("Success")))));

  }

  @NotNull
  private static CDTPPacket ackPayload() {
    CDTPPacket payload = new CDTPPacket();
    payload.setCommand((short) 1000);
    payload.setData(GSON.toJson(Response.ok(ackMessage)).getBytes());
    return payload;
  }

  @Before
  public void init() {
    String host = "127.0.0.1";
    int port = 8099;
    channel = YHCNettyClient.startClient(host, port);
  }

  public void login() throws InterruptedException, InvalidProtocolBufferException {
    CDTPPacket packet = loginPacket(sender, deviceId);
    CDTPPacket result = execute(packet);
    CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(result.getData());
    assertThat(loginResp.getCode()).isEqualTo(200);
  }

  @Test
  public void singleChar() throws InterruptedException {
    CDTPPacket packet = loginPacket(sender, deviceId);
    execute(packet);
    packet = singleChatPacket(sender, receive, message, deviceId);
    CDTPPacket result = execute(packet);
    System.out.println(result);
  }

  private CDTPPacket execute(CDTPPacket reqPacket) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    responseHandler.resetLatch(latch);
    channel.writeAndFlush(reqPacket);
    latch.await();
    return responseHandler.getResult();
  }

}
