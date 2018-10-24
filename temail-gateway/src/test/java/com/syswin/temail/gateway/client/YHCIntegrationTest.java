package com.syswin.temail.gateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.gateway.client.PacketMaker.ackPayload;
import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static com.syswin.temail.gateway.client.PacketMaker.mqMsgPayload;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.gateway.client.YHCIntegrationTest.MQ_SERVER_PORT;
import static com.syswin.temail.gateway.client.YHCIntegrationTest.NAMESRV;
import static com.syswin.temail.gateway.client.YHCIntegrationTest.PORT;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
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
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
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
        "temail.gateway.allIdleTimeSeconds=3",
        "temail.gateway.netty.port=" + PORT,
        "temail.gateway.rocketmq.namesrv-addr=" + NAMESRV + ":" + MQ_SERVER_PORT,
        "temail.gateway.rocketmq.mq-topic=temail-gateway",
    })
@RunWith(SpringRunner.class)
@Ignore
public class YHCIntegrationTest {

  static final int MQ_SERVER_PORT = 9875;
  static final int PORT = 8095;
  static final String NAMESRV = "namesrv";
  @ClassRule
  public static RuleChain rules;
  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8090);
  private static RocketMqNameServerContainer rocketMqNameSrv;
  private static Gson gson = new Gson();
  private static DefaultMQProducer mqProducer = new DefaultMQProducer(uniquify("test-producer-group"));

  static {
    Network NETWORK = Network.newNetwork();
    rocketMqNameSrv = new RocketMqNameServerContainer()
        .withNetwork(NETWORK)
        .withNetworkAliases(NAMESRV)
        .withFixedExposedPort(MQ_SERVER_PORT, 9876);
    RocketMqBrokerContainer rocketMqBroker = new RocketMqBrokerContainer()
        .withNetwork(NETWORK)
        .withEnv("NAMESRV_ADDR", "namesrv:" + MQ_SERVER_PORT)
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
  public static void beforeClass() throws MQClientException, InterruptedException {
    stubFor(post(urlEqualTo("/verify"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(Response.ok())))
    );

    stubFor(any(urlEqualTo("/dispatch"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(ackPayload()))));

    stubFor(any(urlEqualTo("/updateStatus"))
        .willReturn(
            aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(gson.toJson(Response.ok("Success")))));

    createMqTopic();
  }

  private static void createMqTopic() throws MQClientException, InterruptedException {
    Thread.sleep(5000);
    mqProducer.setNamesrvAddr(rocketMqNameSrv.getContainerIpAddress() + ":" + MQ_SERVER_PORT);
    mqProducer.start();
    // ensure topic exists before consumer connects, or no message will be received
    try {
      mqProducer.createTopic(mqProducer.getCreateTopicKey(), "temail-gateway", 1);
    } catch (MQClientException e) {
      //
    }
  }

  @Before
  public void init() {
    if (client == null) {
      client = new YHCNettyClient("127.0.0.1", PORT);
      client.start();
    }
  }

  public void login() throws InvalidProtocolBufferException {
    CDTPPacket packet = loginPacket(sender, deviceId);
    CDTPPacket result = client.syncExecute(packet);
    CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(result.getData());
    assertThat(loginResp.getCode()).isEqualTo(200);
  }

  //  @Test
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
    CDTPPacket reqPacket = singleChatPacket(sender, receive, message, deviceId);
    CDTPPacket respPacket = client.syncExecute(reqPacket);
    assertThat(respPacket.getCommandSpace()).isEqualTo(SINGLE_MESSAGE_CODE);
    assertThat(respPacket.getCommandSpace()).isEqualTo(SEND_MESSAGE.getCode());
    Response response = gson.fromJson(new String(respPacket.getData()), Response.class);
    assertIs2xxSuccessful(response.getCode());

    // 接收消息
    CDTPPacketTrans packet = mqMsgPayload(sender, message);
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
