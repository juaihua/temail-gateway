package com.syswin.temail.gateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static com.syswin.temail.gateway.client.PacketMaker.mqMsgPayload;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SYNC_STATUS_CODE;
import static com.syswin.temail.ps.server.Constants.NOTIFY_COMMAND;
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
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.common.entity.CommandType;
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
        "temail.gateway.updateSocketStatusUrl=http://localhost:8090/locations",
        "temail.gateway.rocketmq.mq-topic=temail-gateway",
        "temail.gateway.netty.read-idle-time-seconds=3000"
    })
@RunWith(SpringRunner.class)
@ActiveProfiles({"debug", "dev"})
public class TemailGatewayTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(8090);
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
  private static final DefaultMQProducer mqProducer = new DefaultMQProducer("test-producer-group");

  private static final Gson GSON = new Gson();
  private static final String ackMessage = uniquify("Sent");
  private static Channel channel;
  private final String sender = "jack@t.email";
  private final String receiver = "sean@t.email";
  private final String message = "hello world";
  private final String deviceId = uniquify("deviceId");
  private final ClientResponseHandler responseHandler = new ClientResponseHandler(() -> loginPacket(sender, deviceId));
  private final NettyClient nettyClient = new NettyClient(responseHandler);
  @Resource
  private TemailGatewayProperties properties;

  @BeforeClass
  public static void beforeClass() throws MQClientException {
    System.setProperty("temail.gateway.namesrvAddr",
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

    stubFor(post(urlEqualTo("/locations"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok("Success")))));

    stubFor(put(urlEqualTo("/locations"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok("Success")))));

    createMqTopic();
  }

  @AfterClass
  public static void afterClass() {
    System.clearProperty("temail.gateway.namesrvAddr");
    mqProducer.shutdown();
  }

  @NotNull
  private static CDTPPacket ackPayload() {
    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace(SINGLE_MESSAGE_CODE);
    payload.setCommand(SEND_MESSAGE.getCode());
    payload.setData(GSON.toJson(Response.ok(ackMessage)).getBytes());
    return payload;
  }

  private static void createMqTopic() throws MQClientException {
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }
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
    await().atMost(30, SECONDS).until(() -> !responseHandler.receivedMessages().isEmpty());
    CDTPPacket packet = responseHandler.receivedMessages().poll();
    assertThat(packet.getCommandSpace()).isEqualTo((CHANNEL_CODE));
    assertThat(packet.getCommand()).isEqualTo(CommandType.LOGIN.getCode());

    CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(packet.getData());
    assertThat(loginResp.getCode()).isEqualTo(200);

    // ack for sent message
    channel.writeAndFlush(singleChatPacket(sender, receiver, message, deviceId));
    await().atMost(30, SECONDS).until(() -> !responseHandler.receivedMessages().isEmpty());
    packet = responseHandler.receivedMessages().poll();
    assertThat(packet.getCommandSpace()).isEqualTo(SINGLE_MESSAGE_CODE);
    assertThat(packet.getCommand()).isEqualTo(SEND_MESSAGE.getCode());

    Response response = GSON.fromJson(new String(packet.getData()), Response.class);
    assertThat(response.getCode()).isEqualTo(200);
    // TODO: 2018/8/31 this needs to be fixed!!
//    assertThat(response.getData()).isEqualTo(ackMessage);

    // receive message from MQ
    mqProducer.send(new Message(properties.getRocketmq().getMqTopic(), properties.getInstance().getMqTag(),
        GSON.toJson(mqMsgPayload(sender, message)).getBytes()), 3000);
    await().atMost(50, SECONDS).until(() -> !responseHandler.receivedMessages().isEmpty());
    packet = responseHandler.receivedMessages().poll();
    assertThat(packet.getCommandSpace()).isEqualTo(SYNC_STATUS_CODE);
    assertThat(packet.getCommand()).isEqualTo(NOTIFY_COMMAND);

    response = GSON.fromJson(new String(packet.getData()), Response.class);
    assertThat(response.getCode()).isEqualTo(200);
    assertThat(response.getData()).isEqualTo(message);
  }

}
