package com.syswin.temail.cdtpserver.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.cdtpserver.entity.CommandEnum.ping;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.CommandEnum;
import com.syswin.temail.cdtpserver.entity.Response;
import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.utils.TemailMqInfBuilder;
import com.syswin.temail.gateway.containers.RocketMqBrokerContainer;
import com.syswin.temail.gateway.containers.RocketMqNameServerContainer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.Resource;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
    "temail.verifyUrl=http://localhost:8090/verify",
    "temail.dispatchUrl=http://localhost:8090/dispatch",
    "temail.updateSocketStatusUrl=http://localhost:8090/updateStatus",
    "temail.allIdleTimeSeconds=3"
})
public class TemailGatewayIntegrationTest {

  private static final Gson GSON = new Gson();

  private static final Network NETWORK = Network.newNetwork();
  private static final RocketMqNameServerContainer rocketMqNameSrv = new RocketMqNameServerContainer()
      .withNetwork(NETWORK)
      .withNetworkAliases("namesrv")
      .withFixedExposedPort(9876, 9876);


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
  private static final String ackMessage = uniquify("Sent");

  private final String sender = "jack@t.email";
  private final String recipient = "sean@t.email";

  private final BlockingQueue<CDTPPackage> receivedPackages = new LinkedBlockingQueue<>();
  private final BlockingQueue<CDTPPackage> toBeSentPackages = new LinkedBlockingQueue<>();
  private final EchoClient client = new EchoClient(sender, "devId", toBeSentPackages, receivedPackages);

  @Resource
  private TemailServerProperties temailServerConfig;
  private TemailMqInfo temailMqInf;


  @BeforeClass
  public static void beforeClass() {
    System.setProperty("temail.namesrvAddr",
        rocketMqNameSrv.getContainerIpAddress() + ":" + 9876);

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

  @AfterClass
  public static void afterClass() {
    System.clearProperty("temail.namesrvAddr");
  }

  @Before
  public void setUp() throws Exception {
    temailMqInf = TemailMqInfBuilder.getTemailMqInf(temailServerConfig);

    mqProducer.setNamesrvAddr(rocketMqNameSrv.getContainerIpAddress() + ":" + 9876);
    mqProducer.setInstanceName("test-producer");
    mqProducer.start();

    client.start("localhost", 8099);
  }

  @After
  public void tearDown() {
    client.stop();

    mqProducer.shutdown();
  }

  @Test
  public void shouldRunFullCycle() throws Exception {
    // append a message to sending queue
    toBeSentPackages.offer(PackageMaker.singleChat(sender, recipient));

    await().atMost(2, SECONDS).until(() -> receivedPackages.peek() != null);

    // login
    CDTPPackage aPackage = receivedPackages.poll();
    assertThat(aPackage.getCommand()).isEqualTo(CommandEnum.connect.getCode());

    // ack for sent message
    await().atMost(2, SECONDS).until(() -> receivedPackages.peek() != null);
    aPackage = receivedPackages.poll();
    assertThat(aPackage.getCommand()).isEqualTo(1000);
    Response response = GSON.fromJson(aPackage.getData().toStringUtf8(), Response.class);
    assertThat(response.getCode()).isEqualTo(200);
    assertThat(response.getData()).isEqualTo(ackMessage);

    System.out.println("*** " + aPackage);

    // receive message from MQ
    mqProducer.send(new Message(temailServerConfig.getMqTopic(), temailMqInf.getMqTag(), GSON.toJson(payload(sender)).getBytes()), 3000);
    await().atMost(5, SECONDS).until(() -> receivedPackages.peek() != null);
    aPackage = receivedPackages.poll();
    assertThat(aPackage.getCommand()).isEqualTo(ping.getCode());
//    assertThat(aPackage.getCommand()).isEqualTo(1000);

//    response = GSON.fromJson(aPackage.getData().toStringUtf8(), Response.class);
//    assertThat(response.getCode()).isEqualTo(200);
//    assertThat(response.getData()).isEqualTo("hello world");
  }

  @NotNull
  private TransferCDTPPackage payload(String recipient) {
    Response<String> body = Response.ok("hello world");
    TransferCDTPPackage payload = new TransferCDTPPackage();
    payload.setCommand(1000);
    payload.setTo(recipient);
    payload.setData(GSON.toJson(body));
    return payload;
  }

  @NotNull
  private static TransferCDTPPackage ackPayload() {
    TransferCDTPPackage payload = new TransferCDTPPackage();
    payload.setCommand(1000);
    payload.setData(GSON.toJson(Response.ok(ackMessage)));
    return payload;
  }
}
