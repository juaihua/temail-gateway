package com.syswin.temail.gateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.gateway.client.PackageMaker.loginPacket;
import static com.syswin.temail.gateway.client.PackageMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandSpaceType.SINGLE_MESSAGE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.containers.RocketMqBrokerContainer;
import com.syswin.temail.gateway.containers.RocketMqNameServerContainer;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.gateway.entity.CommandType;
import com.syswin.temail.gateway.entity.Response;
import io.netty.channel.Channel;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;

@Slf4j
@SpringBootTest(
    properties = {
        "temail.gateway.verifyUrl=http://localhost:8090/verify",
        "temail.gateway.dispatchUrl=http://localhost:8090/dispatch",
        "temail.gateway.updateSocketStatusUrl=http://localhost:8090/updateStatus",
        "temail.gateway.mqTopic=temail-gateway",
        "temail.gateway.allIdleTimeSeconds=3"
    })
@RunWith(SpringRunner.class)
@ActiveProfiles({"debug", "dev"})
public class TemailGatewayTest {
  private static final Network NETWORK = Network.newNetwork();
  private static final int MQ_SERVER_PORT = 9876;
  private static final RocketMqNameServerContainer rocketMqNameSrv = new RocketMqNameServerContainer()
      .withNetwork(NETWORK)
      .withNetworkAliases("namesrv")
      .withFixedExposedPort(MQ_SERVER_PORT, MQ_SERVER_PORT);


  private static final RocketMqBrokerContainer rocketMqBroker = new RocketMqBrokerContainer()
      .withNetwork(NETWORK)
      .withEnv("NAMESRV_ADDR", "namesrv:9876")
      .withFixedExposedPort(10909, 10909)
      .withFixedExposedPort(10911, 10911);

  @ClassRule
  public static final RuleChain RULES = RuleChain.outerRule(rocketMqNameSrv)
      .around(rocketMqBroker);

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(8090);
  private static final DefaultMQProducer mqProducer = new DefaultMQProducer("test-producer-group");

  private static final Gson GSON = new Gson();
  private static final String ackMessage = uniquify("Sent");
  private static Channel channel;
  private final String sender = "jack@t.email";
  private final String receiver = "sean@t.email";
  private final String message = "hello world";
  private final String deviceId = uniquify("deviceId");

  @Resource
  private TemailGatewayProperties properties;

  private final ClientResponseHandler responseHandler = new ClientResponseHandler(() -> loginPacket(sender, deviceId));
  private final NettyClient nettyClient = new NettyClient(responseHandler);

  @BeforeClass
  public static void beforeClass() throws MQClientException {
    System.setProperty("temail.namesrvAddr",
        rocketMqNameSrv.getContainerIpAddress() + ":" + MQ_SERVER_PORT);

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

    createMqTopic();
  }

  @AfterClass
  public static void afterClass() {
    System.clearProperty("temail.namesrvAddr");
    mqProducer.shutdown();
  }

  @NotNull
  private static CDTPPacket ackPayload() {
    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace(SINGLE_MESSAGE.getCode());
    payload.setCommand(SEND_MESSAGE.getCode());
    payload.setData(GSON.toJson(Response.ok(ackMessage)).getBytes());
    return payload;
  }

  @Before
  public void init() {
    channel = nettyClient.start("127.0.0.1", 8099);
  }

  @After
  public void tearDown() {
    nettyClient.stop();
  }

  @Test
  public void shouldRunFullCycle() throws Exception {
    // login
    await().atMost(3, SECONDS).until(() -> !responseHandler.receivedMessages().isEmpty());
    CDTPPacket packet = responseHandler.receivedMessages().poll();
    assertThat(packet.getCommandSpace()).isEqualTo((CHANNEL.getCode()));
    assertThat(packet.getCommand()).isEqualTo(CommandType.LOGIN.getCode());

    CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(packet.getData());
    assertThat(loginResp.getCode()).isEqualTo(200);

    // ack for sent message
    channel.writeAndFlush(singleChatPacket(sender, receiver, message, deviceId));
    await().atMost(3, SECONDS).until(() -> !responseHandler.receivedMessages().isEmpty());
    packet = responseHandler.receivedMessages().poll();
    assertThat(packet.getCommandSpace()).isEqualTo(SINGLE_MESSAGE.getCode());
    assertThat(packet.getCommand()).isEqualTo(SEND_MESSAGE.getCode());

    Response response = GSON.fromJson(new String(packet.getData()), Response.class);
    assertThat(response.getCode()).isEqualTo(200);
    assertThat(response.getData()).isEqualTo(ackMessage);

    // receive message from MQ
    mqProducer.send(new Message(properties.getMqTopic(), properties.getMqTag(), GSON.toJson(mqMsgPayload(sender, message)).getBytes()), 3000);
    await().atMost(5, SECONDS).until(() -> !responseHandler.receivedMessages().isEmpty());
    packet = responseHandler.receivedMessages().poll();
    assertThat(packet.getCommandSpace()).isEqualTo(SINGLE_MESSAGE.getCode());
    assertThat(packet.getCommand()).isEqualTo(SEND_MESSAGE.getCode());

    response = GSON.fromJson(new String(packet.getData()), Response.class);
    assertThat(response.getCode()).isEqualTo(200);
    assertThat(response.getData()).isEqualTo(message);
  }

  private static void createMqTopic() throws MQClientException {
    mqProducer.setNamesrvAddr(rocketMqNameSrv.getContainerIpAddress() + ":" + MQ_SERVER_PORT);
    mqProducer.start();
    // ensure topic exists before consumer connects, or no message will be received
    await().atMost(10, SECONDS).until(() -> {

      try {
        mqProducer.createTopic(mqProducer.getCreateTopicKey(), "temail-gateway", 1);
        return true;
      } catch (MQClientException e) {
        e.printStackTrace();
        return false;
      }
    });
  }

  @NotNull
  private CDTPPacket mqMsgPayload(String recipient, String message) {
    Response<String> body = Response.ok(message);
    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace(SINGLE_MESSAGE.getCode());
    payload.setCommand(SEND_MESSAGE.getCode());
    Header header = new Header();
    header.setReceiver(recipient);
    payload.setHeader(header);
    payload.setData(GSON.toJson(body).getBytes());
    return payload;
  }

}
