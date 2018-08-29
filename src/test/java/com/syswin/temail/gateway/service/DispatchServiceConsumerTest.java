package com.syswin.temail.gateway.service;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.Response;
import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.springframework.web.reactive.function.client.WebClient;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.gateway.entity.CommandSpaceType.SINGLE_MESSAGE;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class DispatchServiceConsumerTest extends ConsumerPactTestMk2 {

  private final String path = "/dispatch";
  private final Gson gson = new Gson();

  private final String sender = "jack@t.email";
  private final String senderOutOfWork = "mike@t.email";
  private final String receiver = "sean@t.email";
  private final String message = "hello world";
  private final String deviceId = uniquify("deviceId");
  private final CDTPPacket packet = singleChatPacket(sender, receiver, message, deviceId);

  private final CDTPPacket packetWithOutOfWork = singleChatPacket(senderOutOfWork, receiver, message, deviceId);

  private static final String ackMessage = uniquify("Sent");

  private Response resultResponse = null;

  private int resultErrorCode;

  private String resultErrorMsg;

  @Before
  public void setUp() {

  }

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, APPLICATION_JSON_VALUE);

    return pactDslWithProvider
        .given("dispatch user request")
        .uponReceiving("dispatch user request to response")
        .method("POST")
        .body(gson.toJson(packet))
        .headers(headers)
        .path(path)
        .willRespondWith()
        .status(200)
        .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
        .body(gson.toJson(Response.ok(OK, ackPayload())))
        .toPact();
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + path;
    WebClient dispatcherWebClient = WebClient.create();
    DispatchService dispatchService = new DispatchService(dispatcherWebClient);
    dispatchService.dispatch(packet, url, new DispatchCallback() {
      @Override
      public Response onsuccess(Response response) {
        resultResponse = response;
        return response;
      }

      @Override
      public void onError(int errorCode, String errorMsg) {
        resultErrorCode = errorCode;
        resultErrorMsg = errorMsg;
      }
    });

    waitAtMost(2, SECONDS).until(() -> resultResponse != null);
    log.info("result code is {},  msg  is {}", resultResponse.getCode(), resultResponse.getMessage());

    assertThat(resultResponse.getCode()).isEqualTo(OK.value());
  }

  @Override
  protected String providerName() {
    return "temail-dispatcher";
  }

  @Override
  protected String consumerName() {
    return "temail-gateway";
  }


  @NotNull
  private CDTPPacket ackPayload() {
    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace(SINGLE_MESSAGE.getCode());
    payload.setCommand(SEND_MESSAGE.getCode());
    payload.setData(gson.toJson(Response.ok(ackMessage)).getBytes());
    return payload;
  }

}
