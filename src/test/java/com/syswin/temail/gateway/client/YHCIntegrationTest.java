package com.syswin.temail.gateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.syswin.temail.gateway.client.PacketMaker.ackPayload;
import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static com.syswin.temail.gateway.client.PacketMaker.mqMsgPayload;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.gateway.entity.CommandSpaceType.SINGLE_MESSAGE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.TemailGatewayApplication;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.containers.RocketMqBrokerContainer;
import com.syswin.temail.gateway.containers.RocketMqNameServerContainer;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.gateway.entity.Response;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;

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
public class YHCIntegrationTest {

  private static final int MQ_SERVER_PORT = 9876;
  @ClassRule
  public static RuleChain rules;
  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8090);
  private static RocketMqNameServerContainer rocketMqNameSrv;
  private static Gson gson = new Gson();
  private static DefaultMQProducer mqProducer = new DefaultMQProducer("test-producer-group");

  static {
    Network NETWORK = Network.newNetwork();
    rocketMqNameSrv = new RocketMqNameServerContainer()
        .withNetwork(NETWORK)
        .withNetworkAliases("namesrv")
        .withFixedExposedPort(MQ_SERVER_PORT, MQ_SERVER_PORT);
    RocketMqBrokerContainer rocketMqBroker = new RocketMqBrokerContainer()
        .withNetwork(NETWORK)
        .withEnv("NAMESRV_ADDR", "namesrv:9876")
        .withFixedExposedPort(10909, 10909)
        .withFixedExposedPort(10911, 10911);
    rules = RuleChain.outerRule(rocketMqNameSrv).around(rocketMqBroker);
  }

  private String sender = "jack@t.email";
  private String receive = "sean@t.email";
  private String deviceId = "deviceId";
  private String message = "hello world";
  private YHCNettyClient client;
  @Resource
  private TemailGatewayProperties properties;

  @BeforeClass
  public static void beforeClass() throws MQClientException {
    stubFor(post(urlEqualTo("/verify"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(Response.ok())))
    );

    stubFor(post(urlEqualTo("/dispatch"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(Response.ok(ackPayload())))));

    stubFor(post(urlEqualTo("/updateStatus"))
        .willReturn(
            aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(gson.toJson(Response.ok("Success")))));

    stubFor(delete(urlEqualTo("/updateStatus"))
        .willReturn(
            aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(gson.toJson(Response.ok("Success")))));

    createMqTopic();
  }

  private static void createMqTopic() throws MQClientException {
    mqProducer.setNamesrvAddr(rocketMqNameSrv.getContainerIpAddress() + ":" + MQ_SERVER_PORT);
    mqProducer.start();
    await().atMost(5, TimeUnit.SECONDS);
    // ensure topic exists before consumer connects, or no message will be received
    mqProducer.createTopic(mqProducer.getCreateTopicKey(), "temail-gateway", 1);
  }

  @Before
  public void init() {
    if (client == null) {
      client = new YHCNettyClient("127.0.0.1", 8099);
      client.start();
    }
  }

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

  @Test
  public void fullCycle() throws Exception {
    // 登录
    CDTPPacket loginReqPacket = loginPacket(sender, deviceId);
    CDTPPacket loginRespPacket = client.syncExecute(loginReqPacket);
    CDTPLoginResp cdtpLoginResp = CDTPLoginResp.parseFrom(loginRespPacket.getData());
    assertIs2xxSuccessful(cdtpLoginResp.getCode());

    // 单聊
    CDTPPacket singleChatReqPacket = singleChatPacket(sender, receive, message, deviceId);
    CDTPPacket singleChatRespPacket = client.syncExecute(singleChatReqPacket);
    assertThat(singleChatRespPacket.getCommandSpace()).isEqualTo(SINGLE_MESSAGE.getCode());
    assertThat(singleChatRespPacket.getCommandSpace()).isEqualTo(SEND_MESSAGE.getCode());
    Response response = gson.fromJson(new String(singleChatRespPacket.getData()), Response.class);
    assertIs2xxSuccessful(response.getCode());

    // TODO(姚华成): 群聊

    // 接收消息
    CDTPPacket packet = mqMsgPayload(sender, message);
    mqProducer.send(new Message(properties.getRocketmq().getMqTopic(), properties.getInstance().getMqTag(),
        gson.toJson(packet).getBytes()), 3000);
    CDTPPacket newResult = client.getNewResult();
    assertThat(newResult.getCommandSpace()).isEqualTo(packet.getCommandSpace());
    assertThat(newResult.getCommand()).isEqualTo(packet.getCommand());
  }

  private void assertIs2xxSuccessful(int code) {
    assertThat(code).isBetween(200, 299);
  }

}
