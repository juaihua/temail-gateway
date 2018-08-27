package com.syswin.temail.gateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.gateway.client.NettyClient.responseHandler;
import static com.syswin.temail.gateway.client.PackageMaker.loginPacket;
import static com.syswin.temail.gateway.client.PackageMaker.singleChat;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.client.EchoClient;
import com.syswin.temail.gateway.TemailGatewayApplication;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CommandType;
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
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 姚华成
 * @date 2018-8-27
 */
@Slf4j
@SpringBootTest(classes = TemailGatewayApplication.class,
    properties = {
        "temail.gateway.verifyUrl=http://localhost:8090/verify",
        "temail.gateway.dispatchUrl=http://localhost:8090/dispatch",
        "temail.gateway.updateSocketStatusUrl=http://localhost:8090/updateStatus",
        "temail.gateway.mqTopic=temail-gateway",
        "temail.gateway.allIdleTimeSeconds=3"
    })
@RunWith(SpringRunner.class)
public class TemailGatewayTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(8090);
  private static final Gson GSON = new Gson();
  private static final String ackMessage = uniquify("Sent");
  private static Channel channel;
  private final String sender = "jack@t.email";
  private final String receive = "sean@t.email";
  private final BlockingQueue<CDTPPacket> receivedPackages = new LinkedBlockingQueue<>();
  private final BlockingQueue<CDTPPacket> toBeSentPackages = new LinkedBlockingQueue<>();
  private final String message = "hello world";
  private EchoClient client;
  @Resource
  private TemailGatewayProperties properties;

  @BeforeClass
  public static void beforeClass() {
    stubFor(post(urlEqualTo("/verify"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok())))
    );

    stubFor(post(urlEqualTo("/dispatch"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok(ackPayload())))));

    stubFor(post(urlEqualTo("/updateStatus"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok("Success")))));

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
    channel = NettyClient.startClient(host, port);
  }

  @Test
  public void testLogin() throws InterruptedException {
    CDTPPacket packet = loginPacket();
    CDTPPacket result = execute(packet);
    assertThat(result.getCommand()).isEqualTo(CommandType.LOGIN_RESP.getCode());
  }

  @Test
  public void testSingleChar() throws InterruptedException {
    CDTPPacket packet = singleChat(sender, receive);
    CDTPPacket result = execute(packet);
    assertThat(result.getCommand()).isEqualTo(CommandType.LOGIN_RESP.getCode());
  }

  private CDTPPacket execute(CDTPPacket reqPacket) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    responseHandler.resetLatch(latch);
    channel.writeAndFlush(reqPacket);
    latch.await();
    return responseHandler.getResult();
  }

}
